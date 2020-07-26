package ju.ma.app.graphql.queries

import com.coxautodev.graphql.tools.GraphQLQueryResolver
import java.util.Optional
import ju.ma.app.EmailTemplate
import ju.ma.app.EmailTemplateRepository
import ju.ma.app.graphql.filters.EmailTemplateWhereInput
import ju.ma.graphql.KeyValuePair
import ju.ma.graphql.PaginationInput
import org.springframework.data.domain.Page
import org.springframework.data.domain.Sort
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Component

@Component
@PreAuthorize("hasAnyAuthority('app:admin', 'read:emails')")
class EmailTemplateQueries(
    private val templates: EmailTemplateRepository
) : GraphQLQueryResolver {
    fun emailTemplatesConnection(page: PaginationInput?, where: EmailTemplateWhereInput?): Page<EmailTemplate> {
        val f: PaginationInput = page ?: PaginationInput()
        if (where == null) {
            return templates.findAll(f.create())
        }
        return templates.findAll(where.build(), f.create())
    }

    fun emailTemplates(where: EmailTemplateWhereInput, orderBy: MutableList<KeyValuePair>?): List<EmailTemplate> {
        return if (orderBy != null) {
            val sort: Sort = Sort.by(orderBy.map { Sort.Order(Sort.Direction.fromString(it.value), it.key) })
            templates.findAll(where.build(), sort).toList()
        } else {
            templates.findAll(where.build()).toList()
        }
    }

    fun emailTemplate(where: EmailTemplateWhereInput): Optional<EmailTemplate> = templates.findOne(where.build())
}
