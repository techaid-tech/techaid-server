package ju.ma.auth

import java.io.IOException
import java.util.TreeMap
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import org.springframework.web.filter.OncePerRequestFilter

private val log = KotlinLogging.logger {}

/**
 * Properties for the CORS configuration
 */
class CorsModelProperties {
    /**
     * The origin of the request. You can use `*` to match all origins
     */
    var origin: String = ""
    /**
     * The requested path to match. You can use `*` to match all paths
     */
    var path: String = ""
    /**
     * A map of all headers to be added to the request
     */
    var headers: Map<String, String> = mutableMapOf()

    private val matcher = AntPathMatcher()

    /**
     * Returns a list of all allowed headers
     */
    val allowedHeaders by lazy {
        headers.asSequence().firstOrNull { (k, _) ->
            k.equals(
                "Access-Control-Allow-Headers",
                ignoreCase = true
            )
        }?.value?.toUpperCase() ?: ""
    }

    /**
     * Looks for the Access-Control-Allow-Methods header
     */
    val allowedMethods by lazy {
        headers.asSequence().firstOrNull { (k, _) ->
            k.equals(
                "Access-Control-Allow-Methods",
                ignoreCase = true
            )
        }?.value?.toUpperCase() ?: ""
    }

    fun matchesOrigin(host: String) = origin == "*" || !origin.isNullOrBlank() && matcher.match(origin, host)

    fun matchesPath(path: String) = path.isNullOrBlank() || path == "*" || matcher.match(path, path)

    fun isMethodAllowed(method: String) = allowedMethods == "*" || allowedMethods.contains(method.toUpperCase())

    fun isHeaderAllowed(value: String): Boolean {
        return allowedHeaders == "*" || value.trim().toUpperCase().split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .all { allowedHeaders.contains(it) }
    }

    override fun toString() = "CorsModelProperties { Path: $path Origin: $origin Headers: $headers }"
}

/**
 * This is the CORS authentication filter. It checks if the currently requested
 * route requires CORS and applies the necessary headers.
 *
 */
@Component
@ConfigurationProperties(prefix = "auth.cors")
@ConditionalOnProperty(value = ["auth.cors.enabled"], matchIfMissing = false)
class CorsFilter : OncePerRequestFilter() {
    /**
     * List of routes with CORS configuration properties
     */
    lateinit var routes: List<CorsModelProperties>

    /**
     * Filter is only applied if the origin header is present
     */
    @Throws(ServletException::class)
    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        return request.getHeader("Origin").isNullOrBlank()
    }

    /**
     * Applies CORS to matching routes
     */
    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val origin = request.getHeader("Origin")?.trim() ?: ""
        val method = request.method.trim().toUpperCase()
        val headers = request.getHeader("Access-Control-Request-Headers")?.trim()?.toUpperCase() ?: ""
        val path = request.requestURI.substring(request.contextPath.length)

        val isPreflightRequest = request.method.trim().toUpperCase() == "OPTIONS"

        val headerTypes = TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER)
        headerTypes["Access-Control-Allow-Headers"] = "pre"
        headerTypes["Access-Control-Allow-Methods"] = "pre"
        headerTypes["Access-Control-Max-Age"] = "pre"
        headerTypes["Access-Control-Allow-Origin"] = "ignore"

        log.debug("Origin: {}", origin)

        for (route in routes) {
            if (route.matchesOrigin(origin) && route.matchesPath(path)) {
                if (!route.isMethodAllowed(method)) {
                    log.debug("Method {} is not in the allowed set {}", method, route.allowedMethods)
                    filterChain.doFilter(request, response)
                    return
                }

                if (!route.isHeaderAllowed(headers)) {
                    log.debug("Header {} is not in the allowed set {}", headers, route.allowedHeaders)
                    filterChain.doFilter(request, response)
                    return
                }

                response.addHeader("Access-Control-Allow-Origin", origin)

                if (isPreflightRequest) {
                    for (header in route.headers) {
                        val type = headerTypes[header.key] ?: ""
                        if (type == "pre") {
                            log.debug("Setting Preflight Header: {} : {}", header.key, header.value)
                            response.addHeader(header.key, header.value)
                        }
                    }

                    response.status = HttpStatus.OK.value()
                    return
                } else {
                    for (header in route.headers) {
                        val type = headerTypes[header.key] ?: ""
                        if (type == "pre" || type == "ignore") {
                            continue
                        }

                        log.debug("Setting Response Header: {} : {}", header.key, header.value)
                        response.addHeader(header.key, header.value)
                    }
                }
            } else {
                log.debug("No Match for {}", route.toString())
            }
        }

        filterChain.doFilter(request, response)
    }
}
