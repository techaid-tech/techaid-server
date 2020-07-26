package ju.ma.app.graphql.mutations

import com.coxautodev.graphql.tools.GraphQLMutationResolver
import javax.validation.Valid
import ju.ma.app.Donor
import ju.ma.app.DonorRepository
import ju.ma.app.Kit
import ju.ma.app.KitRepository
import ju.ma.app.Volunteer
import ju.ma.app.VolunteerRepository
import ju.ma.app.services.LocationService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

fun fetchDonor(repo: DonorRepository, donor: Donor): Donor {
    var existingDonor: Donor? = if (donor.email.trim().isNotBlank()) {
        repo.findByEmail(donor.email) ?: if (donor.phoneNumber.trim().isNotBlank()) {
            repo.findByPhoneNumber(donor.phoneNumber)
        } else null
    } else {
        if (donor.phoneNumber.trim().isNotBlank()) {
            repo.findByPhoneNumber(donor.phoneNumber)
        } else null
    }
    return if (existingDonor != null) {
        repo.save(existingDonor.apply {
            referral = if (donor.referral.isNotBlank()) donor.referral else referral
            postCode = if (donor.postCode.isNotBlank()) donor.postCode else postCode
            email = if (donor.email.isNotBlank()) donor.email else email
            phoneNumber = if (donor.phoneNumber.isNotBlank()) donor.phoneNumber else phoneNumber
        })
    } else {
        repo.save(donor)
    }
}

@Component
class PublicMutations(
    private val donors: DonorRepository,
    private val kits: KitRepository,
    private val volunteers: VolunteerRepository,
    private val locationService: LocationService
) : GraphQLMutationResolver {
    fun createVolunteer(@Valid data: CreateVolunteerInput): Volunteer {
        if (data.email.isNotBlank()) {
            volunteers.findByEmail(data.email)?.let {
                throw IllegalArgumentException("A volunteer with the email address ${data.email} already exits!")
            }
        }

        return volunteers.save(data.entity.apply {
            if (postCode.isNotBlank()) {
                coordinates = locationService.findCoordinates(postCode)
            }
        })
    }

    @Transactional
    fun donateItem(@Valid data: DonateItemInput): DonateItemPayload {
        if (data.kits.isEmpty()) throw IllegalArgumentException("kits cannot be empty")
        var donor = fetchDonor(donors, data.donor.entity).apply {
            if (postCode.isNotBlank()) {
                coordinates = locationService.findCoordinates(postCode)
            }
        }
        val payload = DonateItemPayload(donor)
        data.kits.forEach {
            var kit = kits.save(it.entity.apply {
                if (location.isNotBlank()) {
                    coordinates = locationService.findCoordinates(location)
                }
            })
            donor.addKit(kit)
            payload.kits.add(kit)
        }
        return payload
    }
}

class DonateItemInput(
    val kits: List<CreateKitInput>,
    val donor: CreateDonorInput
)

class DonateItemPayload(
    val donor: Donor,
    val kits: MutableList<Kit> = mutableListOf()
)
