package ju.ma.auth

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class SecretAuthenticationProvider(private val authService: AuthService) : AuthenticationProvider {

    override fun authenticate(authentication: Authentication): Authentication? {
        var auth: SecretAuthenticationToken? = null
        logger.debug("Verifying: $authentication")
        authentication.credentials?.let { token ->
            val user = authService.adminForToken(token.toString())
            user?.let {
                auth = SecretAuthenticationToken(user, token.toString(), user.authorities)
            }
        }

        return auth
    }

    override fun supports(authentication: Class<*>): Boolean {
        return with(authentication) {
            equals(SecretAuthenticationToken::class.java)
        }
    }
}

class SecretAuthenticationToken : AbstractAuthenticationToken {
    private var secret: String? = null
    private val principal: Any?

    constructor(secret: String?) : super(null) {
        this.secret = secret
        this.principal = null
        isAuthenticated = false
    }

    constructor(
        principal: Any?,
        secret: String,
        authorities: Collection<GrantedAuthority>
    ) : super(authorities) {
        this.principal = principal
        this.secret = secret
        super.setAuthenticated(true)
    }

    override fun getCredentials(): Any? {
        return this.secret
    }

    override fun getPrincipal(): Any? {
        return this.principal
    }

    @Throws(IllegalArgumentException::class)
    override fun setAuthenticated(isAuthenticated: Boolean) {
        if (isAuthenticated) {
            throw IllegalArgumentException(
                "Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead"
            )
        }

        super.setAuthenticated(false)
    }

    override fun eraseCredentials() {
        super.eraseCredentials()
        secret = null
    }
}

class SecretAuthenticationFilter : AbstractAuthenticationProcessingFilter(AntPathRequestMatcher("/login", "POST")) {
    override fun attemptAuthentication(request: HttpServletRequest, response: HttpServletResponse): Authentication? {
        val token = request.getParameter("x-admin-token")
        logger.debug("Attempting authentication with token, length: ${token?.length}")
        if (token.isNullOrBlank()) {
            return null
        }

        return SecretAuthenticationToken(token)
    }
}
