package ju.ma.auth

import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Service

@Service
class AuthService {
    @Value("\${auth.admin-header:X-Auth-Admin-Secret}")
    lateinit var adminHeader: String
    @Value("\${auth.admin-secret}")
    private lateinit var secret: String

    fun adminForToken(token: String): AppUser? {
        if (token.isBlank() || secret.isBlank()) {
            return null
        }

        if (token == secret) {
            return AppUser(
                user = User(
                    name = "Admin Token",
                    username = "root"
                ),
                password = token,
                app = "*",
                authorities = listOf(
                    "read:volunteers",
                    "read:kits",
                    "read:donors",
                    "read:users",
                    "write:volunteers",
                    "write:kits",
                    "write:donors",
                    "write:users",
                    "delete:volunteers",
                    "delete:kits",
                    "delete:donors",
                    "delete:users",
                    "read:emails",
                    "write:emails",
                    "read:organisations",
                    "write:organisations",
                    "delete:organisations"
                ).map { SimpleGrantedAuthority("$it") }
                    .toMutableList()
            )
        }

        return null
    }
}
