package ju.ma.app.graphql.filters

import com.github.alexliesenfeld.querydsl.jpa.hibernate.JsonPath
import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.dsl.EnumPath
import com.querydsl.jpa.JPAExpressions
import java.time.Instant
import ju.ma.app.KitStatus
import ju.ma.app.KitType
import ju.ma.app.QKit
import ju.ma.app.QKitVolunteer
import ju.ma.graphql.BooleanComparison
import ju.ma.graphql.IntegerComparision
import ju.ma.graphql.JsonComparison
import ju.ma.graphql.LongComparision
import ju.ma.graphql.TextComparison
import ju.ma.graphql.TimeComparison

class KitStatusComparison(
    /**
     * Matches values equal to
     */
    var _eq: KitStatus? = null,
    /**
     * Matches values greater than
     */
    var _gt: KitStatus? = null,
    /**
     * Matches values greater than or equal to
     */
    var _gte: KitStatus? = null,
    /**
     * Matches values contained in the collection
     */
    var _in: MutableList<KitStatus>? = null,
    /**
     * Matches values that are null
     */
    var _is_null: Boolean? = null,
    /**
     * Matches values less than
     */
    var _lt: KitStatus? = null,
    /**
     * Matches values less than or equal to
     */
    var _lte: KitStatus? = null,
    /**
     * Matches values not equal to
     */
    var _neq: KitStatus? = null,
    /**
     * Matches values not in the collection
     */
    var _nin: MutableList<KitStatus>? = null
) {
    /**
     * Returns a filter for the specified [path]
     */
    fun build(path: EnumPath<KitStatus>): BooleanBuilder {
        val builder = BooleanBuilder()
        _eq?.let { builder.and(path.eq(it)) }
        _gt?.let { builder.and(path.gt(it)) }
        _gte?.let { builder.and(path.goe(it)) }
        _in?.let { builder.and(path.`in`(it)) }
        _is_null?.let {
            if (it) {
                builder.and(path.isNull)
            } else {
                builder.and(path.isNotNull)
            }
        }
        _lt?.let { builder.and(path.lt(it)) }
        _lte?.let { builder.and(path.loe(it)) }
        _neq?.let { builder.and(path.ne(it)) }
        _nin?.let { builder.and(path.notIn(it)) }
        return builder
    }
}

class KitTypeComparison(
    /**
     * Matches values equal to
     */
    var _eq: KitType? = null,
    /**
     * Matches values greater than
     */
    var _gt: KitType? = null,
    /**
     * Matches values greater than or equal to
     */
    var _gte: KitType? = null,
    /**
     * Matches values contained in the collection
     */
    var _in: MutableList<KitType>? = null,
    /**
     * Matches values that are null
     */
    var _is_null: Boolean? = null,
    /**
     * Matches values less than
     */
    var _lt: KitType? = null,
    /**
     * Matches values less than or equal to
     */
    var _lte: KitType? = null,
    /**
     * Matches values not equal to
     */
    var _neq: KitType? = null,
    /**
     * Matches values not in the collection
     */
    var _nin: MutableList<KitType>? = null
) {
    /**
     * Returns a filter for the specified [path]
     */
    fun build(path: EnumPath<KitType>): BooleanBuilder {
        val builder = BooleanBuilder()
        _eq?.let { builder.and(path.eq(it)) }
        _gt?.let { builder.and(path.gt(it)) }
        _gte?.let { builder.and(path.goe(it)) }
        _in?.let { builder.and(path.`in`(it)) }
        _is_null?.let {
            if (it) {
                builder.and(path.isNull)
            } else {
                builder.and(path.isNotNull)
            }
        }
        _lt?.let { builder.and(path.lt(it)) }
        _lte?.let { builder.and(path.loe(it)) }
        _neq?.let { builder.and(path.ne(it)) }
        _nin?.let { builder.and(path.notIn(it)) }
        return builder
    }
}

class KitAttributesWhereInput(
    var otherType: TextComparison? = null,
    var pickup: TextComparison? = null,
    var state: TextComparison? = null,
    var consent: TextComparison? = null,
    var notes: TextComparison? = null,
    var status: TextComparison? = null,
    var pickupAvailability: TextComparison? = null,
    var filters: List<JsonComparison>? = null,
    var AND: MutableList<KitAttributesWhereInput> = mutableListOf(),
    var OR: MutableList<KitAttributesWhereInput> = mutableListOf(),
    var NOT: MutableList<KitAttributesWhereInput> = mutableListOf()
) {
    fun build(entity: QKit = QKit.kit): BooleanBuilder {
        val builder = BooleanBuilder()
        val json = JsonPath.of(entity.attributes)

        otherType?.let { builder.and(it.build(json.get("otherType").asText())) }
        pickup?.let { builder.and(it.build(json.get("pickup").asText())) }
        state?.let { builder.and(it.build(json.get("state").asText())) }
        consent?.let { builder.and(it.build(json.get("consent").asText())) }
        notes?.let { builder.and(it.build(json.get("notes").asText())) }
        pickupAvailability?.let { builder.and(it.build(json.get("pickupAvailability").asText())) }
        status?.let { builder.and(it.build(json.get("status").asText())) }
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

class KitWhereInput(
    var id: LongComparision? = null,
    var location: TextComparison? = null,
    var status: KitStatusComparison? = null,
    var type: KitTypeComparison? = null,
    var age: IntegerComparision? = null,
    var model: TextComparison? = null,
    var archived: BooleanComparison? = null,
    var createdAt: TimeComparison<Instant>? = null,
    var updatedAt: TimeComparison<Instant>? = null,
    var attributes: KitAttributesWhereInput? = null,
    var volunteer: VolunteerWhereInput? = null,
    var organisation: OrganisationWhereInput? = null,
    var donor: DonorWhereInput? = null,
    var AND: MutableList<KitWhereInput> = mutableListOf(),
    var OR: MutableList<KitWhereInput> = mutableListOf(),
    var NOT: MutableList<KitWhereInput> = mutableListOf()
) {
    fun build(entity: QKit = QKit.kit): BooleanBuilder {
        val builder = BooleanBuilder()
        age?.let { builder.and(it.build(entity.age)) }
        id?.let { builder.and(it.build(entity.id)) }
        status?.let { builder.and(it.build(entity.status)) }
        type?.let { builder.and(it.build(entity.type)) }
        model?.let { builder.and(it.build(entity.model)) }
        location?.let { builder.and(it.build(entity.location)) }
        createdAt?.let { builder.and(it.build(entity.createdAt)) }
        archived?.let { builder.and(it.build(entity.archived)) }
        updatedAt?.let { builder.and(it.build(entity.updatedAt)) }
        attributes?.let { builder.and(it.build(entity)) }
        organisation?.let { builder.and(it.build(entity.organisation)) }
        volunteer?.let {
            builder.and(JPAExpressions.selectOne().from(entity.volunteers, QKitVolunteer.kitVolunteer)
                .where(it.build(QKitVolunteer.kitVolunteer.volunteer)).exists())
        }
        donor?.let { builder.and(it.build(entity.donor)) }

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
