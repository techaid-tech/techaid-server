package ju.ma.auth

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

/**
 * A representation of a user in the system. Users that aren't logged in
 * are marked as anonymous with the anonymous role
 */
class AppUser(
    var user: User,
    @JsonIgnore
    private var password: String = "",
    var app: String = "",
    private val authorities: MutableList<SimpleGrantedAuthority> = mutableListOf()
) : UserDetails {
    companion object {
        /**
         * A reference to the anonymous user object
         */
        val anonymous = AppUser(
            user = User(
                name = "anonymous",
                username = "anonymous"
            ),
            app = "anonymous",
            authorities = mutableListOf(SimpleGrantedAuthority("ANONYMOUS"))
        )
    }

    /**
     * Returns the list of authorities granted to this user
     */
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> = authorities

    /**
     * Defaults to true as we are using JWT tokens
     */
    override fun isEnabled() = user.locked.not()

    /**
     * Returns the username attached to this user
     */
    override fun getUsername(): String {
        if (user.username.isNullOrBlank()) {
            return user.email
        }

        return user.username
    }

    /**
     * Defaults to true as we are using JWT tokens.
     */
    override fun isCredentialsNonExpired() = user.expired.not()

    /**
     * The password for the current user. Defaults to the JWT token
     */
    override fun getPassword() = password

    /**
     * Defaults to true as we are using JWT tokens.
     */
    override fun isAccountNonExpired() = user.expired.not()

    /**
     * Defaults to true as we are using JWT tokens.
     */
    override fun isAccountNonLocked() = user.locked.not()

    /**
     * Returns true of the user is anonymous
     */
    fun isAnonymous(): Boolean {
        return username == "anonymous" && hasAuthority("ANONYMOUS")
    }

    fun hasAuthority(authority: String): Boolean {
        return authorities.firstOrNull { it.authority == authority } != null
    }
}

class User(
    var name: String,
    var username: String,
    var email: String = "",
    var locked: Boolean = false,
    var expired: Boolean = false
)
