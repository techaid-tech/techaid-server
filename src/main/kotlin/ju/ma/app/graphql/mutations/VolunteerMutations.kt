package ju.ma.app.graphql.mutations

import com.coxautodev.graphql.tools.GraphQLMutationResolver
import javax.persistence.EntityNotFoundException
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import ju.ma.app.Capacity
import ju.ma.app.QVolunteer
import ju.ma.app.Volunteer
import ju.ma.app.VolunteerAttributes
import ju.ma.app.VolunteerRepository
import ju.ma.app.services.FilterService
import ju.ma.app.services.LocationService
import ju.ma.toNullable
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated

@Component
@Validated
@PreAuthorize("hasAnyAuthority('write:volunteers')")
@Transactional
class VolunteerMutations(
    private val volunteers: VolunteerRepository,
    private val locationService: LocationService,
    private val filterService: FilterService
) : GraphQLMutationResolver {
    fun updateVolunteer(@Valid data: UpdateVolunteerInput): Volunteer {
        val entity =
            volunteers.findOne(filterService.volunteerFilter().and(QVolunteer.volunteer.id.eq(data.id))).toNullable()
                ?: throw EntityNotFoundException("Unable to locate a volunteer with id: ${data.id}")
        if (filterService.hasAuthority("read:volunteers:assigned")) {
            if (entity.email != data.email) {
                throw IllegalArgumentException("Unable to update your email address to ${data.email}")
            }
        }

        return data.apply(entity).apply {
            if (postCode.isNotBlank() && (coordinates == null || coordinates?.input != postCode)) {
                coordinates = locationService.findCoordinates(postCode)
            }
        }
    }

    @PreAuthorize("hasAnyAuthority('delete:volunteers')")
    fun deleteVolunteer(id: Long): Boolean {
        val volunteer =
            volunteers.findOne(filterService.volunteerFilter().and(QVolunteer.volunteer.id.eq(id))).toNullable()
                ?: throw EntityNotFoundException("Unable to locate volunteer with id: $id")
        volunteers.delete(volunteer)
        return true
    }
}

data class CreateVolunteerInput(
    @get:NotBlank
    val name: String = "",
    val email: String = "",
    val storage: String = "",
    val phoneNumber: String = "",
    val expertise: String = "",
    val subGroup: String = "",
    val postCode: String = "",
    val availability: String = "",
    val transport: String = "",
    val consent: String = "",
    val attributes: VolunteerAttributesInput? = null
) {
    val entity by lazy {
        val volunteer = Volunteer(
            name = name,
            phoneNumber = phoneNumber,
            email = email,
            expertise = expertise,
            subGroup = subGroup,
            postCode = postCode,
            availability = availability,
            storage = storage,
            transport = transport,
            consent = consent
        )
        attributes?.apply(volunteer)
        volunteer
    }
}

data class VolunteerAttributesInput(
    var dropOffAvailability: String? = null,
    var hasCapacity: Boolean? = null,
    var accepts: List<String>? = null,
    var capacity: CapacityInput? = null
) {
    fun apply(entity: Volunteer): VolunteerAttributes {
        val self = this
        if (entity.attributes == null) {
            entity.attributes = VolunteerAttributes()
        }
        return entity.attributes.apply {
            accepts = self.accepts ?: listOf()
            dropOffAvailability = self.dropOffAvailability ?: ""
            hasCapacity = self.hasCapacity ?: false
            capacity = self.capacity?.entity ?: Capacity()
        }
    }
}

data class CapacityInput(
    val phones: Int? = 0,
    val tablets: Int? = 0,
    val laptops: Int? = 0,
    val allInOnes: Int? = 0,
    val other: Int? = 0
) {
    val entity by lazy {
        Capacity(
            phones = phones ?: 0,
            tablets = tablets ?: 0,
            laptops = laptops ?: 0,
            allInOnes = allInOnes ?: 0,
            other = other ?: 0
        )
    }
}

data class UpdateVolunteerInput(
    @get:NotNull
    val id: Long,
    @get:NotBlank
    val name: String = "",
    val storage: String = "",
    var transport: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val expertise: String? = "",
    val subGroup: String = "",
    val postCode: String = "",
    val availability: String = "",
    val consent: String? = "",
    val attributes: VolunteerAttributesInput? = null
) {
    fun apply(entity: Volunteer): Volunteer {
        val self = this
        return entity.apply {
            name = self.name
            phoneNumber = if (phoneNumber != self.phoneNumber) self.phoneNumber else phoneNumber
            email = if (email != self.email) self.email else this.email
            expertise = self.expertise ?: ""
            transport = self.transport
            subGroup = self.subGroup
            postCode = self.postCode
            availability = self.availability
            storage = self.storage
            consent = self.consent ?: ""
            self.attributes?.apply(this)
        }
    }
}
