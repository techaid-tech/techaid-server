package ju.ma.app.graphql.queries

import com.coxautodev.graphql.tools.GraphQLQueryResolver
import ju.ma.app.services.Coordinates
import ju.ma.app.services.LocationService
import org.springframework.stereotype.Component

@Component
class GlobalQueries(private val locationService: LocationService) : GraphQLQueryResolver {
    fun location(address: String): Coordinates? {
        return locationService.findCoordinates(address)
    }
}
