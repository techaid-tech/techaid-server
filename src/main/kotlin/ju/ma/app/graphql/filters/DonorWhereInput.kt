package ju.ma.app.graphql.filters

import com.querydsl.core.BooleanBuilder
import java.time.Instant
import ju.ma.app.QDonor
import ju.ma.graphql.LongComparision
import ju.ma.graphql.TextComparison
import ju.ma.graphql.TimeComparison

class DonorWhereInput(
    var id: LongComparision? = null,
    var postCode: TextComparison? = null,
    var phoneNumber: TextComparison? = null,
    var name: TextComparison? = null,
    var email: TextComparison? = null,
    var referral: TextComparison? = null,
    var createdAt: TimeComparison<Instant>? = null,
    var updatedAt: TimeComparison<Instant>? = null,
    var AND: MutableList<DonorWhereInput> = mutableListOf(),
    var OR: MutableList<DonorWhereInput> = mutableListOf(),
    var NOT: MutableList<DonorWhereInput> = mutableListOf()
) {
    fun build(entity: QDonor = QDonor.donor): BooleanBuilder {
        val builder = BooleanBuilder()
        id?.let { builder.and(it.build(entity.id)) }
        phoneNumber?.let { builder.and(it.build(entity.phoneNumber)) }
        email?.let { builder.and(it.build(entity.email)) }
        referral?.let { builder.and(it.build(entity.referral)) }
        postCode?.let { builder.and(it.build(entity.postCode)) }
        createdAt?.let { builder.and(it.build(entity.createdAt)) }
        updatedAt?.let { builder.and(it.build(entity.updatedAt)) }
        name?.let { builder.and(it.build(entity.name)) }
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
