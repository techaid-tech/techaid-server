package ju.ma.app.graphql.queries

import com.coxautodev.graphql.tools.GraphQLQueryResolver
import com.coxautodev.graphql.tools.GraphQLResolver
import java.util.Optional
import ju.ma.app.Volunteer
import ju.ma.app.VolunteerRepository
import ju.ma.app.graphql.filters.VolunteerWhereInput
import ju.ma.app.services.FilterService
import ju.ma.graphql.KeyValuePair
import ju.ma.graphql.PaginationInput
import org.springframework.data.domain.Page
import org.springframework.data.domain.Sort
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Component

@Component
@PreAuthorize("hasAnyAuthority('read:volunteers', 'read:volunteers:assigned')")
class VolunteerQueries(
    private val volunteers: VolunteerRepository,
    private val filterService: FilterService
) : GraphQLQueryResolver {
    fun volunteersConnection(page: PaginationInput?, where: VolunteerWhereInput?): Page<Volunteer> {
        val filter = filterService.volunteerFilter()
        val f: PaginationInput = page ?: PaginationInput()
        if (where == null) {
            return volunteers.findAll(filter, f.create())
        }
        return volunteers.findAll(filter.and(where.build()), f.create())
    }

    fun volunteers(where: VolunteerWhereInput, orderBy: MutableList<KeyValuePair>?): List<Volunteer> {
        val filter = filterService.volunteerFilter()
        return if (orderBy != null) {
            val sort: Sort = Sort.by(orderBy.map { Sort.Order(Sort.Direction.fromString(it.value), it.key) })
            volunteers.findAll(filter.and(where.build()), sort).toList()
        } else {
            volunteers.findAll(filter.and(where.build())).toList()
        }
    }

    fun volunteer(where: VolunteerWhereInput): Optional<Volunteer> =
        volunteers.findOne(filterService.volunteerFilter().and(where.build()))
}

@Component
class VolunteerResolver : GraphQLResolver<Volunteer> {
    companion object {
        val EMAIL_MASK = Regex("(?<=.)[^@](?=[^@]*[^@]@)|(?:(?<=@.)|(?!^)\\G(?=[^@]*$)).(?!$)")
    }

    fun email(entity: Volunteer): String {
        return entity.email
    }

    fun phoneNumber(entity: Volunteer): String {
        return entity.phoneNumber
    }
}
