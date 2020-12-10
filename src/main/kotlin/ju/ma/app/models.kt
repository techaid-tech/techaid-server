package ju.ma.app

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.vladmihalcea.hibernate.type.json.JsonBinaryType
import com.vladmihalcea.hibernate.type.json.JsonStringType
import java.io.Serializable
import java.time.Instant
import java.util.Objects
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.MappedSuperclass
import javax.persistence.MapsId
import javax.persistence.OneToMany
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import ju.ma.app.services.Coordinates
import org.apache.commons.lang3.RandomStringUtils
import org.hibernate.annotations.Formula
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.hibernate.annotations.TypeDefs
import org.hibernate.annotations.UpdateTimestamp

@TypeDefs(
    TypeDef(name = "json", typeClass = JsonStringType::class),
    TypeDef(name = "jsonb", typeClass = JsonBinaryType::class)
)
@MappedSuperclass
class BaseEntity

@Entity
@Table(name = "volunteers")
class Volunteer(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "volunteer-seq-generator")
    @SequenceGenerator(name = "volunteer-seq-generator", sequenceName = "volunteer_sequence", allocationSize = 1)
    var id: Long = 0,
    var name: String,
    var phoneNumber: String,
    var email: String,
    var expertise: String,
    var subGroup: String,
    var storage: String,
    var transport: String,
    var postCode: String,
    var availability: String,
    var createdAt: Instant = Instant.now(),
    var consent: String,
    @Formula("""
        (SELECT COUNT(*) FROM kit_volunteers k where k.volunteer_id = id)
    """)
    var kitCount: Int = 0,
    @UpdateTimestamp
    var updatedAt: Instant = Instant.now(),
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    var coordinates: Coordinates? = null,
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    var attributes: VolunteerAttributes = VolunteerAttributes(),
    @JsonIgnore
    @OneToMany(mappedBy = "volunteer", fetch = FetchType.LAZY, orphanRemoval = true, cascade = [CascadeType.ALL])
    var kits: MutableSet<KitVolunteer> = mutableSetOf(),
    @OneToMany(
        mappedBy = "volunteer",
        fetch = FetchType.LAZY,
        cascade = [CascadeType.ALL],
        orphanRemoval = false
    )
    var organisations: MutableSet<Organisation> = mutableSetOf()
) : BaseEntity() {
    fun addOrganisation(org: Organisation) {
        organisations.add(org)
        org.volunteer = this
    }

    fun removeOrganisation(org: Organisation) {
        organisations.removeIf {
            if (org == it) {
                org.volunteer = null
                true
            } else {
                false
            }
        }
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class VolunteerAttributes(
    var dropOffAvailability: String = "",
    var hasCapacity: Boolean = false,
    var accepts: List<String> = listOf(),
    var capacity: Capacity = Capacity()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Capacity(
    val phones: Int = 0,
    val tablets: Int = 0,
    val laptops: Int = 0,
    val allInOnes: Int = 0,
    val other: Int = 0,
    val chromebooks: Int? = 0
)

@Entity
@Table(name = "donors")
class Donor(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "donor-seq-generator")
    @SequenceGenerator(name = "donor-seq-generator", sequenceName = "donor_sequence", allocationSize = 1)
    var id: Long = 0,
    var postCode: String,
    var phoneNumber: String,
    var email: String,
    var name: String,
    var referral: String,
    var createdAt: Instant = Instant.now(),
    @Formula("""
        ( SELECT COUNT(*) FROM kits k where k.donor_id = id )
    """)
    var kitCount: Int = 0,
    @UpdateTimestamp
    var updatedAt: Instant = Instant.now(),
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    var coordinates: Coordinates? = null,
    @OneToMany(
        mappedBy = "donor",
        fetch = FetchType.LAZY,
        cascade = [CascadeType.ALL],
        orphanRemoval = false
    )
    var kits: MutableSet<Kit> = mutableSetOf()
) : BaseEntity() {
    fun addKit(kit: Kit) {
        kits.add(kit)
        kit.donor = this
    }

    fun removeKit(kit: Kit) {
        kits.removeIf {
            if (kit == it) {
                kit.donor = null
                true
            } else {
                false
            }
        }
    }
}

@Entity
@Table(name = "kits")
class Kit(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "kit-seq-generator")
    @SequenceGenerator(name = "kit-seq-generator", sequenceName = "kit_sequence", allocationSize = 1)
    var id: Long = 0,
    @Enumerated(EnumType.STRING)
    var type: KitType = KitType.OTHER,
    @Enumerated(EnumType.STRING)
    var status: KitStatus = KitStatus.NEW,
    var model: String,
    var location: String,
    var age: Int,
    @Type(type = "yes_no")
    var archived: Boolean = false,
    var createdAt: Instant = Instant.now(),
    @UpdateTimestamp
    var updatedAt: Instant = Instant.now(),
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    var attributes: KitAttributes = KitAttributes(),
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    var coordinates: Coordinates? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donor_id")
    var donor: Donor? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisation_id")
    var organisation: Organisation? = null,
    @JsonIgnore
    @OneToMany(mappedBy = "kit", fetch = FetchType.LAZY, orphanRemoval = true, cascade = [CascadeType.ALL])
    var volunteers: MutableSet<KitVolunteer> = mutableSetOf()
) : BaseEntity() {
    fun addVolunteer(volunteer: Volunteer, type: KitVolunteerType) {
        val entity = KitVolunteer(this, volunteer, KitVolunteerId(this.id, volunteer.id, type))
        volunteers.add(entity)
        volunteer.kits.add(entity)
    }

    fun replaceVolunteers(entities: Iterable<Volunteer>, type: KitVolunteerType): List<Volunteer> {
        val incoming = entities.map { it.id to it }.toMap()
        val existing = volunteers.filter { it.id.type == type }
            .map { it.id.volunteerId to it }.toMap()
        existing.forEach { (k, v) ->
            if (!incoming.containsKey(k)) {
                removeVolunteer(v.volunteer, type)
            }
        }
        val added = mutableListOf<Volunteer>()
        incoming.forEach { (k, v) ->
            if (!existing.containsKey(k)) {
                addVolunteer(v, type)
                added.add(v)
            }
        }
        return added
    }

    fun removeVolunteer(type: KitVolunteerType): Boolean {
        return volunteers.removeIf { kv ->
            if (kv.id.type == type) {
                kv.volunteer.kits.remove(kv)
                true
            } else {
                false
            }
        }
    }

    fun removeVolunteer(volunteer: Volunteer, type: KitVolunteerType): Boolean {
        return volunteers.removeIf { kv ->
            if (kv.id.type == type && kv.kit == this && kv.volunteer == volunteer) {
                volunteer.kits.remove(kv)
                true
            } else {
                false
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Kit) return false
        return id != 0L && id == other.id
    }

    override fun hashCode() = 13
}

@JsonIgnoreProperties(ignoreUnknown = true)
class KitAttributes(
    var images: MutableList<KitImage> = mutableListOf(),
    var otherType: String? = null,
    var pickup: String = "",
    var state: String = "",
    var consent: String = "",
    var notes: String = "",
    var pickupAvailability: String? = null,
    var credentials: String? = null,
    var status: List<String> = listOf(),
    var network: String? = null,
    var otherNetwork: String? = "UNKNOWN"
)

@JsonIgnoreProperties(ignoreUnknown = true)
class KitImage(
    val image: String,
    val id: String = RandomStringUtils.random(5, true, true)
)

enum class KitType {
    OTHER,
    LAPTOP,
    DESKTOP,
    TABLET,
    SMARTPHONE,
    ALLINONE,
    CHROMEBOOK
}

enum class KitStatus {
    NEW,
    ASSESSMENT_NEEDED,
    ACCEPTED,
    DECLINED,
    DROPOFF_AGGREED,
    DROPOFF_PENDING,
    PICKUP_SCHEDULED,
    WITH_TECHIE,
    UPDATE_FAILED,
    READY,
    ALLOCATED,
    DELIVERY_ARRANGED,
    DELIVERED,
    INCOMPLETE,
    RECYCLED
}

enum class KitVolunteerType { LOGISTICS, ORGANISER, TECHNICIAN }

@Embeddable
class KitVolunteerId(
    @Column(name = "course_id")
    var kitId: Long,
    @Column(name = "student_id")
    var volunteerId: Long,
    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    var type: KitVolunteerType
) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        if (other !is KitVolunteerId) return false
        return type == other.type && kitId == other.kitId && volunteerId == other.volunteerId
    }

    override fun hashCode(): Int {
        return Objects.hash(kitId, volunteerId, type)
    }

    override fun toString(): String {
        return "KitVolunteerId(type=$type, kitId=$kitId, volunteerId=$volunteerId)"
    }
}

@Entity
@Table(name = "kit_volunteers")
class KitVolunteer(
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("kitId")
    var kit: Kit,
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("volunteerId")
    var volunteer: Volunteer,
    @EmbeddedId
    var id: KitVolunteerId,
    var createdAt: Instant = Instant.now()
) {
    val type get() = id.type
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        other ?: return false
        if (other !is KitVolunteer) return false
        if (id != other.id) return false
        return kit == other.kit && volunteer == other.volunteer
    }

    override fun hashCode(): Int {
        return Objects.hash(kit, volunteer, id)
    }

    override fun toString(): String {
        return "KitVolunteer(id=$id, enrolledAt=$createdAt)"
    }
}

@Entity
@Table(name = "email_templates")
class EmailTemplate(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "email-template-seq-generator")
    @SequenceGenerator(
        name = "email-template-seq-generator",
        sequenceName = "email_template_sequence",
        allocationSize = 1
    )
    var id: Long = 0,
    var body: String,
    var subject: String,
    var active: Boolean = true,
    var createdAt: Instant = Instant.now(),
    @UpdateTimestamp
    var updatedAt: Instant = Instant.now()
)

@Entity
@Table(name = "organisations")
class Organisation(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "organisation-seq-generator")
    @SequenceGenerator(
        name = "organisation-seq-generator",
        sequenceName = "organisation_sequence",
        allocationSize = 1
    )
    var id: Long = 0,
    var name: String,
    var website: String,
    var contact: String,
    var phoneNumber: String,
    var email: String,
    var createdAt: Instant = Instant.now(),
    @Formula("""
        (SELECT COUNT(*) FROM kits k where k.organisation_id = id)
    """)
    var kitCount: Int = 0,
    @UpdateTimestamp
    var updatedAt: Instant = Instant.now(),
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    var attributes: OrganisationAttributes = OrganisationAttributes(),
    @Type(type = "yes_no")
    var archived: Boolean = false,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "volunteer_id")
    var volunteer: Volunteer? = null,
    @OneToMany(
        mappedBy = "organisation",
        fetch = FetchType.LAZY,
        cascade = [CascadeType.ALL],
        orphanRemoval = false
    )
    var kits: MutableSet<Kit> = mutableSetOf()
) {
    fun addKit(kit: Kit) {
        kits.add(kit)
        kit.organisation = this
    }

    fun removeKit(kit: Kit) {
        kits.removeIf {
            if (kit == it) {
                kit.organisation = null
                true
            } else {
                false
            }
        }
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class OrganisationAttributes(
    var request: Capacity = Capacity(),
    var alternateRequest: Capacity = Capacity(),
    var accepts: List<String> = listOf(),
    var alternateAccepts: List<String> = listOf(),
    var notes: String = ""
)
