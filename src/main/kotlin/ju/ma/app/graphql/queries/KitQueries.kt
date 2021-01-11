package ju.ma.app.graphql.queries

import com.coxautodev.graphql.tools.GraphQLQueryResolver
import com.coxautodev.graphql.tools.GraphQLResolver
import java.util.Optional
import ju.ma.app.DeviceImage
import ju.ma.app.Kit
import ju.ma.app.KitAttributes
import ju.ma.app.KitRepository
import ju.ma.app.KitStatusCount
import ju.ma.app.KitTypeCount
import ju.ma.app.graphql.filters.KitWhereInput
import ju.ma.app.services.FilterService
import ju.ma.graphql.KeyValuePair
import ju.ma.graphql.PaginationInput
import org.springframework.data.domain.Page
import org.springframework.data.domain.Sort
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Component

@Component
@PreAuthorize("hasAnyAuthority('read:kits', 'read:kits:assigned')")
class KitQueries(
    private val kits: KitRepository,
    private val filterService: FilterService
) : GraphQLQueryResolver {
    fun statusCount(): List<KitStatusCount> {
        return kits.statusCount()
    }

    fun typeCount(): List<KitTypeCount> {
        return kits.typeCount()
    }

    fun kitsConnection(page: PaginationInput?, where: KitWhereInput?): Page<Kit> {
        val f: PaginationInput = page ?: PaginationInput()
        val filter = filterService.kitFilter()
        if (where == null) {
            return kits.findAll(filter, f.create())
        }
        return kits.findAll(filter.and(where.build()), f.create())
    }

    fun kits(where: KitWhereInput, orderBy: MutableList<KeyValuePair>?): List<Kit> {
        val filter = filterService.kitFilter()
        return if (orderBy != null) {
            val sort: Sort = Sort.by(orderBy.map { Sort.Order(Sort.Direction.fromString(it.value), it.key) })
            kits.findAll(filter.and(where.build()), sort).toList()
        } else {
            kits.findAll(filter.and(where.build())).toList()
        }
    }

    fun kit(where: KitWhereInput): Optional<Kit> = kits.findOne(filterService.kitFilter().and(where.build()))
}

@Component
class kitResolver : GraphQLResolver<KitAttributes> {
    fun getAttributes(kit: Kit): KitAttributes {
        val attr = kit.attributes
        attr.kit = kit
        return attr
    }

    fun getImages(attr: KitAttributes): List<DeviceImage> {
        return attr?.kit?.images?.images ?: listOf()
    }
}
