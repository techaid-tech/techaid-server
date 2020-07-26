package ju.ma.auth

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.IOException
import java.util.LinkedHashMap
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.GenericFilterBean

/**
 * Filter to intercept JWT Tokens from the header and create a user session
 */
class TokenAuthenticationFilter(private val authService: AuthService) : GenericFilterBean() {
    /**
     * Verifies JWT token from the http-request header
     */
    @Throws(IOException::class, ServletException::class)
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val httpRequest = request as HttpServletRequest
        val httpResponse = response as HttpServletResponse
        var accessToken = request.getHeader(authService.adminHeader) ?: ""

        if (accessToken.isNotBlank()) {
            var admin = authService.adminForToken(accessToken)
            if (admin == null) {
                haltRequest(
                    httpResponse, httpRequest,
                    "Invalid superuser token specified in ${authService.adminHeader}",
                    "INVALID_ADMIN_TOKEN"
                )
                return
            }
            val authentication = UsernamePasswordAuthenticationToken(
                admin,
                accessToken,
                admin.authorities
            )
            SecurityContextHolder.getContext().authentication = authentication
            chain.doFilter(request, response)
            return
        }
        chain.doFilter(request, response)
    }

    private fun loggedIn(): Boolean {
        val context = SecurityContextHolder.getContext()
        return context.authentication != null && context.authentication.isAuthenticated
    }

    private fun haltRequest(
        httpResponse: HttpServletResponse,
        httpRequest: HttpServletRequest,
        message: String?,
        type: String
    ) {
        val responseBody = LinkedHashMap<String, Any?>()
        httpResponse.status = HttpServletResponse.SC_UNAUTHORIZED
        httpResponse.addHeader("Content-Type", "application/json;charset=utf-8")

        responseBody["url"] = httpRequest.requestURI
        responseBody["method"] = httpRequest.method
        responseBody["message"] = message
        responseBody["type"] = type
        httpResponse.writer.write(ObjectMapper().writeValueAsString(responseBody))
        httpResponse.writer.flush()
        httpResponse.writer.close()
    }
}
