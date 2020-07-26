package ju.ma.app.graphql

import graphql.servlet.GraphQLContext
import graphql.servlet.GraphQLContextBuilder
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.websocket.Session
import javax.websocket.server.HandshakeRequest
import org.dataloader.DataLoader
import org.dataloader.DataLoaderRegistry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * An interface for holding all available GraphQL data Loaders. The loaders
 * are responsible for lazy loading data in batched requests
 */
interface GraphQLDataLoader<K, V> {
    /**
     * The name of this loader
     */
    val name: String

    /**
     * The actual loader responsible for batch fetching the data
     */
    fun loader(): DataLoader<K, V>
}

/**
 * A custom context builder. Allows us to build custom contexts for use
 * with each graphql request to avoid N+1 query issues
 */
@Component
class CustomGraphQLContextBuilder : GraphQLContextBuilder {
    /**
     * List of dataLoaders that will be applied to every request
     */
    @Autowired(required = false)
    var dataLoaders: List<GraphQLDataLoader<*, *>> = mutableListOf()

    /**
     * Builds the graphql context for http requests
     */
    override fun build(req: HttpServletRequest, response: HttpServletResponse): GraphQLContext {
        val context = GraphQLContext(req, response)
        context.setDataLoaderRegistry(buildDataLoaderRegistry())

        return context
    }

    /**
     * Builds the graphql context
     */
    override fun build(): GraphQLContext {
        val context = GraphQLContext()
        context.setDataLoaderRegistry(buildDataLoaderRegistry())
        return context
    }

    /**
     * Builds the graphql context for web socket subscription requests
     */
    override fun build(session: Session, request: HandshakeRequest): GraphQLContext {
        val context = GraphQLContext(session, request)
        context.setDataLoaderRegistry(buildDataLoaderRegistry())
        return context
    }

    private fun buildDataLoaderRegistry(): DataLoaderRegistry {
        val dataLoaderRegistry = DataLoaderRegistry()
        dataLoaders.forEach {
            dataLoaderRegistry.register(it.name, it.loader())
        }
        return dataLoaderRegistry
    }
}
