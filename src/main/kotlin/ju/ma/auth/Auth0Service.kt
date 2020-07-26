package ju.ma.auth

import com.auth0.client.auth.AuthAPI
import com.auth0.client.mgmt.ManagementAPI
import com.auth0.client.mgmt.filter.RolesFilter
import com.auth0.client.mgmt.filter.UserFilter
import com.auth0.json.mgmt.Role
import com.auth0.json.mgmt.RolesPage
import com.auth0.json.mgmt.users.User
import com.auth0.json.mgmt.users.UsersPage
import java.time.Instant
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class Auth0Service {
    @Value("\${auth0.domain}")
    private lateinit var domain: String

    @Value("\${auth0.client-id}")
    private lateinit var clientId: String

    @Value("\${auth0.client-secret}")
    private lateinit var clientSecret: String

    private var _mgmt: ManagementAPI? = null

    private var mgmtApiExpires: Long = 0

    private val auth by lazy {
        AuthAPI(domain, clientId, clientSecret)
    }

    val mgmt: ManagementAPI
        get() {
            if (_mgmt != null && mgmtApiExpires > Instant.now().epochSecond + 10) {
                return _mgmt!!
            }
            val request = auth.requestToken("https://$domain/api/v2/")
            val holder = request.execute()
            mgmtApiExpires = Instant.now().epochSecond + holder.expiresIn
            _mgmt = ManagementAPI(domain, holder.accessToken)
            return _mgmt!!
        }

    fun findAllUsers(filter: UserFilter = UserFilter()): UsersPage {
        return mgmt.users().list(filter).execute()
    }

    fun resetPassword(email: String) {
        auth.resetPassword(email, "Username-Password-Authentication").execute()
    }

    fun deleteById(id: String) {
        mgmt.users().delete(id).execute()
    }

    fun findById(id: String, filter: UserFilter = UserFilter()): User {
        return mgmt.users().get(id, filter).execute()
    }

    fun signUp(email: String, username: String, password: String, fields: Map<String, String> = mapOf()) {
        auth.signUp(email, username, password, "Username-Password-Authentication")
            .setCustomFields(fields)
            .execute()
    }

    fun create(user: User): User {
        return mgmt.users().create(user).execute()
    }

    fun update(id: String, user: User): User {
        return mgmt.users().update(id, user).execute()
    }

    fun findRoles(filter: RolesFilter = RolesFilter()): RolesPage {
        return mgmt.roles().list(filter).execute()
    }

    fun findRoleById(roleId: String): Role {
        return mgmt.roles().get(roleId).execute()
    }

    fun assignRoles(roleId: String, userIds: List<String>): Role {
        mgmt.roles().assignUsers(roleId, userIds).execute()
        return findRoleById(roleId)
    }

    fun removeRoles(userId: String, roleIds: List<String>): User {
        mgmt.users().removeRoles(userId, roleIds).execute()
        return findById(userId)
    }
}
