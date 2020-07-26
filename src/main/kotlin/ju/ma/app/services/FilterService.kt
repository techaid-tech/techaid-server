package ju.ma.app.services

import com.querydsl.core.BooleanBuilder
import com.querydsl.jpa.JPAExpressions
import ju.ma.app.QDonor
import ju.ma.app.QKit
import ju.ma.app.QKitVolunteer
import ju.ma.app.QVolunteer
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Service

@Service
class FilterService {
    val currentUser: JwtAuthenticationToken?
        get() {
            val auth = SecurityContextHolder.getContext().authentication ?: return null
            return if (auth is JwtAuthenticationToken) {
                auth
            } else null
        }

    fun authenticated(): Boolean {
        val user = currentUser ?: return false
        return user.authorities.isNotEmpty()
    }

    fun userDetails(): OAuthUser {
        currentUser?.let { user ->
            val name = user.tokenAttributes["https://lambeth-techaid.ju.ma/name"]?.toString() ?: ""
            val email = user.tokenAttributes["https://lambeth-techaid.ju.ma/email"]?.toString() ?: ""
            return OAuthUser(name.trim(), email.trim())
        }
        return OAuthUser("", "")
    }

    fun kitFilter(): BooleanBuilder {
        val filter = BooleanBuilder()
        val user = currentUser ?: return filter
        if (hasAuthority("admin:kits")) {
            return filter
        }
        if (hasAuthority("read:kits:assigned")) {
            user.tokenAttributes["https://lambeth-techaid.ju.ma/email"]?.toString()?.let { email ->
                if (!email.isNullOrBlank()) {
                    return filter.and(
                        JPAExpressions.selectOne().from(QKit.kit.volunteers, QKitVolunteer.kitVolunteer)
                            .where(QKitVolunteer.kitVolunteer.volunteer.email.eq(email)).exists()
                    )
                }
            }
        }
        return filter
    }

    fun volunteerFilter(): BooleanBuilder {
        val filter = BooleanBuilder()
        val user = currentUser ?: return filter
        if (hasAuthority("admin:volunteers")) {
            return filter
        }
        if (hasAuthority("read:volunteers:assigned")) {
            user.tokenAttributes["https://lambeth-techaid.ju.ma/email"]?.toString()?.let { email ->
                if (!email.isNullOrBlank()) {
                    return filter.and(
                        QVolunteer.volunteer.email.eq(email)
                    )
                }
            }
        }
        return filter
    }

    fun donorFilter(): BooleanBuilder {
        val filter = BooleanBuilder()
        val user = currentUser ?: return filter
        if (hasAuthority("admin:donors")) {
            return filter
        }
        if (hasAuthority("read:donors:assigned")) {
            user.tokenAttributes["https://lambeth-techaid.ju.ma/email"]?.toString()?.let { email ->
                if (!email.isNullOrBlank()) {
                    return filter.and(
                        JPAExpressions.selectOne().from(QDonor.donor.kits, QKit.kit)
                            .where(
                                JPAExpressions.selectOne().from(QKit.kit.volunteers, QKitVolunteer.kitVolunteer)
                                    .where(QKitVolunteer.kitVolunteer.volunteer.email.eq(email)).exists()
                            ).exists()
                    )
                }
            }
        }
        return filter
    }

    fun hasAuthority(permission: String): Boolean {
        val user = currentUser ?: return false
        return user.authorities.any { it.authority == permission }
    }
}

data class OAuthUser(val name: String, val email: String) {
    val empty: Boolean by lazy {
        name.trim().isBlank() && email.trim().isBlank()
    }

    val identifier by lazy {
        if (name.trim().isBlank()) {
            email
        } else if (name != email && email.trim().isNotBlank()) {
            "$name<$email>"
        } else {
            ""
        }
    }
}
