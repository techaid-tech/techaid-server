package ju.ma.app.graphql.mutations

import com.coxautodev.graphql.tools.GraphQLMutationResolver
import javax.persistence.EntityNotFoundException
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import ju.ma.app.DonorRepository
import ju.ma.app.ImageRepository
import ju.ma.app.Kit
import ju.ma.app.KitAttributes
import ju.ma.app.KitRepository
import ju.ma.app.KitStatus
import ju.ma.app.KitType
import ju.ma.app.KitVolunteerType
import ju.ma.app.Organisation
import ju.ma.app.OrganisationRepository
import ju.ma.app.QKit
import ju.ma.app.Volunteer
import ju.ma.app.VolunteerRepository
import ju.ma.app.services.FilterService
import ju.ma.app.services.LocationService
import ju.ma.app.services.MailService
import ju.ma.app.services.createEmail
import ju.ma.toNullable
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated

@Component
@Validated
@PreAuthorize("hasAnyAuthority('write:kits')")
@Transactional
class KitMutations(
    private val kits: KitRepository,
    private val donors: DonorRepository,
    private val organisations: OrganisationRepository,
    private val volunteers: VolunteerRepository,
    private val locationService: LocationService,
    private val filterService: FilterService,
    private val mailService: MailService,
    private val imgRepo: ImageRepository
) : GraphQLMutationResolver {

    fun createKit(@Valid data: CreateKitInput): Kit {
        val details = filterService.userDetails()
        val volunteer = if (details.email.isNotBlank()) {
            volunteers.findByEmail(details.email)
        } else {
            null
        }
        val kit = kits.save(data.entity.apply {
            if (location.isNotBlank()) {
                coordinates = locationService.findCoordinates(location)
            }
        })
        volunteer?.let { kit.addVolunteer(it, KitVolunteerType.ORGANISER) }
        return kit
    }

    fun updateKit(@Valid data: UpdateKitInput): Kit {
        val self = this
        val entity = kits.findOne(filterService.kitFilter().and(QKit.kit.id.eq(data.id))).toNullable()
            ?: throw EntityNotFoundException("Unable to locate a kit with id: ${data.id}")
        val organisers = entity.volunteers.filter { it.type == KitVolunteerType.ORGANISER }.map { it.id.volunteerId }
        val logistics = entity.volunteers.filter { it.type == KitVolunteerType.LOGISTICS }.map { it.id.volunteerId }
        val technicians = entity.volunteers.filter { it.type == KitVolunteerType.TECHNICIAN }.map { it.id.volunteerId }

        val previousStatus = entity.status
        return data.apply(entity).apply {
            if (location.isNotBlank() && (coordinates == null || coordinates?.input != location)) {
                coordinates = locationService.findCoordinates(location)
            }

            if (previousStatus != this.status) {
                notifyStatus(entity.volunteers.map { it.volunteer }, this, previousStatus)
            }

            if (data.organiserIds.isNullOrEmpty()) {
                removeVolunteer(KitVolunteerType.ORGANISER)
            } else if (data.organiserIds != organisers) {
                notifyAssigned(
                    replaceVolunteers(
                        self.volunteers.findAllById(data.organiserIds),
                        KitVolunteerType.ORGANISER
                    ), this, KitVolunteerType.ORGANISER
                )
            }

            if (data.logisticIds.isNullOrEmpty()) {
                removeVolunteer(KitVolunteerType.LOGISTICS)
            } else if (data.logisticIds != logistics) {
                notifyAssigned(
                    replaceVolunteers(
                        self.volunteers.findAllById(data.logisticIds),
                        KitVolunteerType.LOGISTICS
                    ), this, KitVolunteerType.LOGISTICS
                )
            }

            if (data.technicianIds.isNullOrEmpty()) {
                removeVolunteer(KitVolunteerType.TECHNICIAN)
            } else if (data.technicianIds != technicians) {
                notifyAssigned(
                    replaceVolunteers(
                        self.volunteers.findAllById(data.technicianIds),
                        KitVolunteerType.TECHNICIAN
                    ), this, KitVolunteerType.TECHNICIAN
                )
            }

            if (data.donorId == null) {
                donor?.removeKit(this)
            } else if (data.donorId != donor?.id) {
                val user = donors.findById(data.donorId).toNullable()
                    ?: throw EntityNotFoundException("Unable to locate a donor with id: ${data.donorId}")
                user.addKit(this)
            }

            if (data.organisationId == null) {
                organisation?.removeKit(this)
            } else if (data.organisationId != organisation?.id) {
                val org = organisations.findById(data.organisationId).toNullable()
                    ?: throw EntityNotFoundException("Unable to locate an organisation with id: ${data.organisationId}")
                org.addKit(this)
                // notifyOrganisation(this.volunteers.map { it.volunteer }, this, org)
            }
        }
    }

    fun notifyAssigned(volunteers: List<Volunteer>, kit: Kit, type: KitVolunteerType) {
        val user = filterService.userDetails()
        volunteers.filter { it.email.isNotBlank() && it.email != user.email }.forEach { v ->
            val msg = createEmail(
                to = v.email,
                from = mailService.address,
                subject = "Lambeth Techaid: Device Assigned",
                bodyText = """
                    Hi ${v.name},
                    
                    ${user.name} assigned you to the ${kit.type} device (${kit.model}) https://app.techaid.ju.ma/dashboard/devices/${kit.id} as a { ${type.name} }.
                    
                    Lambeth Techaid
                """.trimIndent(),
                mimeType = "plain",
                charset = "UTF-8"
            )
            try {
                mailService.sendMessage(msg)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun notifyStatus(volunteers: List<Volunteer>, kit: Kit, previousStatus: KitStatus) {
        val user = filterService.userDetails()
        volunteers.filter { it.email.isNotBlank() && it.email != user.email }.forEach { v ->
            val msg = createEmail(
                to = v.email,
                from = mailService.address,
                subject = "Lambeth Techaid: Device Status Updated",
                bodyText = """
                    Hi ${v.name},
                    
                    ${user.name} updated the status of ${kit.type} device (${kit.model}) https://app.techaid.ju.ma/dashboard/devices/${kit.id} 
                    from { $previousStatus } to ${kit.status}
                    
                    Lambeth Techaid
                """.trimIndent(),
                mimeType = "plain",
                charset = "UTF-8"
            )
            try {
                mailService.sendMessage(msg)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun notifyOrganisation(volunteers: List<Volunteer>, kit: Kit, org: Organisation) {
        val user = filterService.userDetails()
        volunteers.filter { it.email.isNotBlank() && it.email != user.email }.forEach { v ->
            val msg = createEmail(
                to = v.email,
                from = mailService.address,
                subject = "Lambeth Techaid: Device Assigned to Organisation",
                bodyText = """
                    Hi ${v.name},
                    
                    ${user.name} assigned the ${kit.type} device (${kit.model}) https://app.techaid.ju.ma/dashboard/devices/${kit.id} 
                    to the organisation { ${org.name} } to https://app.techaid.ju.ma/dashboard/organisations/${org.id} 
                    
                    Lambeth Techaid
                """.trimIndent(),
                mimeType = "plain",
                charset = "UTF-8"
            )
            try {
                mailService.sendMessage(msg)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @PreAuthorize("hasAnyAuthority('delete:kits')")
    fun deleteKit(id: Long): Boolean {
        val kit = kits.findOne(filterService.kitFilter().and(QKit.kit.id.eq(id))).toNullable()
            ?: throw EntityNotFoundException("Unable to locate a kit with id: $id")
        kits.delete(kit)
        return true
    }
}

data class CreateKitInput(
    val type: KitType,
    val otherType: String? = null,
    val status: KitStatus? = null,
    @get:NotBlank
    val model: String = "",
    @get:NotBlank
    val location: String,
    val age: Int,
    val attributes: KitAttributesInput
) {
    val entity by lazy {
        val kit = Kit(
            type = type,
            status = status ?: KitStatus.NEW,
            model = model,
            location = location,
            age = age
        )
        kit.attributes = attributes.apply(kit)
        kit
    }
}

data class KitImageInput(val image: String? = null, val url: String? = null, val id: String? = null)

data class KitAttributesInput(
    val images: MutableList<KitImageInput>?,
    val otherType: String? = null,
    val state: String,
    val consent: String,
    val pickup: String,
    val notes: String? = null,
    val pickupAvailability: String? = null,
    val credentials: String? = null,
    val status: List<String>? = null,
    val network: String? = null,
    val otherNetwork: String? = null
) {
    fun apply(entity: Kit): KitAttributes {
        val self = this
        // val inputImages = images ?: listOf<KitImageInput>()
        // val kitImages = entity.images ?: KitImage(entity)
        // val imageMap = kitImages.images.map { it.id to it }.toMap().toMutableMap()
        // val newMap = inputImages.filter { !it.id.isNullOrBlank() }.map { it.id!! to it }.toMap()
        // imageMap.keys.forEach {
        //     if (!newMap.containsKey(it)) {
        //         imageMap.remove(it)
        //     }
        // }
        // inputImages?.forEach {
        //     if (it.image != null) {
        //         val img = DeviceImage(image = it.image)
        //         imageMap[img.id] = img
        //     }
        // }

        // if (imageMap.isNotEmpty()) {
        //     kitImages.images = imageMap.values.toMutableList()
        //     entity.images = kitImages
        // } else {
        //     entity.images = null
        // }

        return entity.attributes.apply {
            otherType = self.otherType
            state = self.state
            consent = self.consent
            pickup = self.pickup
            pickupAvailability = self.pickupAvailability
            status = self.status ?: status
            credentials = self.credentials
            notes = self.notes ?: notes
            network = self.network ?: "UNKNOWN"
        }
    }
}

data class UpdateKitInput(
    @get:NotNull
    val id: Long,
    val type: KitType,
    val status: KitStatus,
    @get:NotBlank
    val model: String = "",
    @get:NotBlank
    val location: String,
    val age: Int,
    val attributes: KitAttributesInput,
    val organiserIds: List<Long>? = null,
    val technicianIds: List<Long>? = null,
    val logisticIds: List<Long>? = null,
    val donorId: Long? = null,
    val organisationId: Long? = null,
    val archived: Boolean? = null
) {
    fun apply(entity: Kit): Kit {
        val self = this
        return entity.apply {
            type = self.type
            status = self.status
            model = self.model
            location = self.location
            age = self.age
            attributes = self.attributes.apply(entity)
            archived = self.archived ?: archived
        }
    }
}
