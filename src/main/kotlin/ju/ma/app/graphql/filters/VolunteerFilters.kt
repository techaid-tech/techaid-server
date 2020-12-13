package ju.ma.app.graphql.filters

import com.github.alexliesenfeld.querydsl.jpa.hibernate.JsonPath
import com.querydsl.core.BooleanBuilder
import java.time.Instant
import ju.ma.app.QVolunteer
import ju.ma.graphql.BooleanComparison
import ju.ma.graphql.IntegerComparision
import ju.ma.graphql.JsonComparison
import ju.ma.graphql.LongComparision
import ju.ma.graphql.TextComparison
import ju.ma.graphql.TimeComparison

class VolunteerWhereInput(
    var id: LongComparision? = null,
    var name: TextComparison? = null,
    var phoneNumber: TextComparison? = null,
    var email: TextComparison? = null,
    var expertise: TextComparison? = null,
    var subGroup: TextComparison? = null,
    var storage: TextComparison? = null,
    var transport: TextComparison? = null,
    var postCode: TextComparison? = null,
    var availability: TextComparison? = null,
    var createdAt: TimeComparison<Instant>? = null,
    var updatedAt: TimeComparison<Instant>? = null,
    var attributes: VolunteerAttributesWhereInput? = null,
    var AND: MutableList<VolunteerWhereInput> = mutableListOf(),
    var OR: MutableList<VolunteerWhereInput> = mutableListOf(),
    var NOT: MutableList<VolunteerWhereInput> = mutableListOf()
) {
    fun build(entity: QVolunteer = QVolunteer.volunteer): BooleanBuilder {
        val builder = BooleanBuilder()
        id?.let { builder.and(it.build(entity.id)) }
        email?.let { builder.and(it.build(entity.email)) }
        name?.let { builder.and(it.build(entity.name)) }
        createdAt?.let { builder.and(it.build(entity.createdAt)) }
        updatedAt?.let { builder.and(it.build(entity.updatedAt)) }
        phoneNumber?.let { builder.and(it.build(entity.phoneNumber)) }
        expertise?.let { builder.and(it.build(entity.expertise)) }
        subGroup?.let { builder.and(it.build(entity.subGroup)) }
        storage?.let { builder.and(it.build(entity.storage)) }
        transport?.let { builder.and(it.build(entity.transport)) }
        postCode?.let { builder.and(it.build(entity.postCode)) }
        availability?.let { builder.and(it.build(entity.availability)) }
        attributes?.let { builder.and(it.build(entity)) }
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

class VolunteerAttributesWhereInput(
    var dropOffAvailability: TextComparison? = null,
    var hasCapacity: BooleanComparison? = null,
    var capacity: VolunteerCapacityWhereInput? = null,
    var filters: List<JsonComparison>? = null,
    var AND: MutableList<VolunteerAttributesWhereInput> = mutableListOf(),
    var OR: MutableList<VolunteerAttributesWhereInput> = mutableListOf(),
    var NOT: MutableList<VolunteerAttributesWhereInput> = mutableListOf()
) {
    fun build(entity: QVolunteer = QVolunteer.volunteer): BooleanBuilder {
        val builder = BooleanBuilder()
        val json = JsonPath.of(entity.attributes)

        dropOffAvailability?.let { builder.and(it.build(json.get("dropOffAvailability").asText())) }
        capacity?.let { builder.and(it.build(entity)) }
        hasCapacity?.let { builder.and(it.build(json.get("hasCapacity").asBool())) }
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

class VolunteerCapacityWhereInput(
    var phones: IntegerComparision? = null,
    var tablets: IntegerComparision? = null,
    var laptops: IntegerComparision? = null,
    var allInOnes: IntegerComparision? = null,
    var desktops: IntegerComparision? = null,
    var filters: List<JsonComparison>? = null,
    var AND: MutableList<VolunteerCapacityWhereInput> = mutableListOf(),
    var OR: MutableList<VolunteerCapacityWhereInput> = mutableListOf(),
    var NOT: MutableList<VolunteerCapacityWhereInput> = mutableListOf()
) {
    fun build(entity: QVolunteer = QVolunteer.volunteer): BooleanBuilder {
        val builder = BooleanBuilder()
        val json = JsonPath.of(entity.attributes)

        phones?.let { builder.and(it.build(json.get("capacity.phones").asInt())) }
        tablets?.let { builder.and(it.build(json.get("capacity.tablets").asInt())) }
        laptops?.let { builder.and(it.build(json.get("capacity.laptops").asInt())) }
        allInOnes?.let { builder.and(it.build(json.get("capacity.allInOnes").asInt())) }
        desktops?.let { builder.and(it.build(json.get("capacity.desktops").asInt())) }
        filters?.let { filter ->
            filter.forEach { builder.and(it.build(json.get("capacity"))) }
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
