package ju.ma.app.config

import ju.ma.auth.AuthService
import ju.ma.auth.CorsFilter
import ju.ma.auth.SecretAuthenticationFilter
import ju.ma.auth.TokenAuthenticationFilter
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtDecoders
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import org.springframework.security.web.session.SessionManagementFilter

private val logger = KotlinLogging.logger {}

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
class SecurityConfig(
    private val corsFilter: CorsFilter,
    private val authService: AuthService
) {
    @Value("\${auth0.audience}")
    private var audience: String = ""

    @Value("\${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    var issuer: String = ""

    @Bean
    fun jwtDecoder(): JwtDecoder {
        val jwtDecoder = JwtDecoders.fromOidcIssuerLocation(issuer) as NimbusJwtDecoder
        val audienceValidator = AudienceValidator(audience)
        val withIssuer = JwtValidators.createDefaultWithIssuer(issuer)
        val withAudience = DelegatingOAuth2TokenValidator(withIssuer, audienceValidator)
        jwtDecoder.setJwtValidator(withAudience)
        return jwtDecoder
    }

    fun secretAuthenticationFilter(manager: AuthenticationManager): SecretAuthenticationFilter {
        val filter = SecretAuthenticationFilter()
        filter.setAuthenticationManager(manager)
        filter.setAuthenticationFailureHandler(SimpleUrlAuthenticationFailureHandler("/login?error=true"))
        return filter
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity, manager: AuthenticationManager): SecurityFilterChain {
        http.csrf().disable()
        http.headers().httpStrictTransportSecurity().disable()
        http.addFilterBefore(corsFilter, SessionManagementFilter::class.java)
        http.addFilterBefore(TokenAuthenticationFilter(authService), BasicAuthenticationFilter::class.java)
        http.addFilterBefore(secretAuthenticationFilter(manager), UsernamePasswordAuthenticationFilter::class.java)
        http.oauth2ResourceServer().jwt().jwtAuthenticationConverter(Auth0TokenConverter())
        http.authorizeRequests()
            .anyRequest().permitAll()
            .and()
            .formLogin()
            .loginPage("/login")
            .defaultSuccessUrl("/login", true)
            .failureUrl("/login?error")
        return http.build();
    }
}

class AudienceValidator(private val audience: String) : OAuth2TokenValidator<Jwt> {
    override fun validate(jwt: Jwt): OAuth2TokenValidatorResult {
        val error = OAuth2Error("invalid_token", "The required audience is missing", null)
        return if (jwt.audience.contains(audience)) {
            OAuth2TokenValidatorResult.success()
        } else OAuth2TokenValidatorResult.failure(error)
    }
}

class Auth0TokenConverter : Converter<Jwt, AbstractAuthenticationToken> {
    private val converter = JwtGrantedAuthoritiesConverter()
    override fun convert(jwt: Jwt): AbstractAuthenticationToken {
        val authorities = converter.convert(jwt)!!
        val permissions = jwt.claims["permissions"]
        if (permissions is List<*>) {
            permissions.forEach {
                if (it is String) {
                    authorities.add(SimpleGrantedAuthority(it))
                }
            }
        }
        return JwtAuthenticationToken(jwt, authorities)
    }
}
