package ju.ma.app.graphql.filters

import com.querydsl.core.BooleanBuilder
import java.time.Instant
import ju.ma.app.QEmailTemplate
import ju.ma.graphql.BooleanComparison
import ju.ma.graphql.LongComparision
import ju.ma.graphql.TextComparison
import ju.ma.graphql.TimeComparison

class EmailTemplateWhereInput(
    var id: LongComparision? = null,
    var active: BooleanComparison? = null,
    var body: TextComparison? = null,
    var subject: TextComparison? = null,
    var createdAt: TimeComparison<Instant>? = null,
    var updatedAt: TimeComparison<Instant>? = null,
    var AND: MutableList<EmailTemplateWhereInput> = mutableListOf(),
    var OR: MutableList<EmailTemplateWhereInput> = mutableListOf(),
    var NOT: MutableList<EmailTemplateWhereInput> = mutableListOf()
) {
    fun build(entity: QEmailTemplate = QEmailTemplate.emailTemplate): BooleanBuilder {
        val builder = BooleanBuilder()
        id?.let { builder.and(it.build(entity.id)) }
        active?.let { builder.and(it.build(entity.active)) }
        body?.let { builder.and(it.build(entity.body)) }
        createdAt?.let { builder.and(it.build(entity.createdAt)) }
        updatedAt?.let { builder.and(it.build(entity.updatedAt)) }
        subject?.let { builder.and(it.build(entity.subject)) }

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
