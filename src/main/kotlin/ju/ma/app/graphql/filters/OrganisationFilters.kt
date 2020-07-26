package ju.ma.app.graphql.filters

import com.github.alexliesenfeld.querydsl.jpa.hibernate.JsonPath
import com.querydsl.core.BooleanBuilder
import java.time.Instant
import ju.ma.app.QOrganisation
import ju.ma.graphql.BooleanComparison
import ju.ma.graphql.JsonComparison
import ju.ma.graphql.LongComparision
import ju.ma.graphql.TextComparison
import ju.ma.graphql.TimeComparison

class OrganisationWhereInput(
    var id: LongComparision? = null,
    var website: TextComparison? = null,
    var phoneNumber: TextComparison? = null,
    var contact: TextComparison? = null,
    var name: TextComparison? = null,
    var archived: BooleanComparison? = null,
    var volunteer: VolunteerWhereInput? = null,
    var email: TextComparison? = null,
    var createdAt: TimeComparison<Instant>? = null,
    var updatedAt: TimeComparison<Instant>? = null,
    var attributes: OrganisationAttributesWhereInput? = null,
    var AND: MutableList<OrganisationWhereInput> = mutableListOf(),
    var OR: MutableList<OrganisationWhereInput> = mutableListOf(),
    var NOT: MutableList<OrganisationWhereInput> = mutableListOf()
) {
    fun build(entity: QOrganisation = QOrganisation.organisation): BooleanBuilder {
        val builder = BooleanBuilder()
        id?.let { builder.and(it.build(entity.id)) }
        name?.let { builder.and(it.build(entity.name)) }
        contact?.let { builder.and(it.build(entity.contact)) }
        email?.let { builder.and(it.build(entity.email)) }
        createdAt?.let { builder.and(it.build(entity.createdAt)) }
        updatedAt?.let { builder.and(it.build(entity.updatedAt)) }
        phoneNumber?.let { builder.and(it.build(entity.phoneNumber)) }
        website?.let { builder.and(it.build(entity.website)) }
        attributes?.let { builder.and(it.build(entity)) }
        archived?.let { builder.and(it.build(entity.archived)) }
        volunteer?.let { builder.and(it.build(entity.volunteer)) }

        if (AND.isNotEmpty()) {
            AND.forEach {
                builder.and(it.build(entity))
            }
        }

        if (OR.isNotEmpty()) {
            OR.forEach {
                builder.or(it.build(entity))
            }
        }

        if (NOT.isNotEmpty()) {
            NOT.forEach {
                builder.andNot(it.build(entity))
            }
        }
        return builder
    }
}

class OrganisationAttributesWhereInput(
    var filters: List<JsonComparison>? = null,
    var AND: MutableList<OrganisationAttributesWhereInput> = mutableListOf(),
    var OR: MutableList<OrganisationAttributesWhereInput> = mutableListOf(),
    var NOT: MutableList<OrganisationAttributesWhereInput> = mutableListOf()
) {
    fun build(entity: QOrganisation = QOrganisation.organisation): BooleanBuilder {
        val builder = BooleanBuilder()
        val json = JsonPath.of(entity.attributes)
        filters?.let { filter ->
            filter.forEach { builder.and(it.build(json)) }
        }
        if (AND.isNotEmpty()) {
            AND.forEach {
                builder.and(it.build(entity))
            }
        }

        if (OR.isNotEmpty()) {
            OR.forEach {
                builder.or(it.build(entity))
            }
        }

        if (NOT.isNotEmpty()) {
            NOT.forEach {
                builder.andNot(it.build(entity))
            }
        }
        return builder
    }
}
