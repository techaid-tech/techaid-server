package ju.ma.app.graphql

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.coxautodev.graphql.tools.GraphQLQueryResolver
import java.util.Date
import java.util.concurrent.TimeUnit
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Component

@PreAuthorize("hasAnyAuthority('read:emails')")
@Component
class MetabaseGraph : GraphQLQueryResolver {
    @Value("\${metabase.secret-key}")
    lateinit var secretKey: String
    @Value("\${metabase.site-url}")
    lateinit var siteUrl: String

    fun metabaseDashboard(ids: List<Long>): List<MetabaseDashboardPayload> {
        return ids.map {
            val token = JWT.create()
                .withClaim("resource", mapOf("dashboard" to it))
                .withClaim("params", mapOf<String, Any>())
                .withExpiresAt(Date((Date().time + TimeUnit.MINUTES.toMillis(30))))
                .sign(
                    Algorithm.HMAC256(secretKey)
                )
            MetabaseDashboardPayload(it, token, "$siteUrl/embed/dashboard/$token")
        }
    }
}

data class MetabaseDashboardPayload(val id: Long, val token: String, val url: String = "")
