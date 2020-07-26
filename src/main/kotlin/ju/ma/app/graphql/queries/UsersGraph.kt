package ju.ma.app.graphql.queries

import com.auth0.client.mgmt.filter.PageFilter
import com.auth0.client.mgmt.filter.RolesFilter
import com.auth0.client.mgmt.filter.UserFilter
import com.auth0.json.mgmt.PermissionsPage
import com.auth0.json.mgmt.Role
import com.auth0.json.mgmt.RolesPage
import com.auth0.json.mgmt.users.User
import com.auth0.json.mgmt.users.UsersPage
import com.coxautodev.graphql.tools.GraphQLMutationResolver
import com.coxautodev.graphql.tools.GraphQLQueryResolver
import com.coxautodev.graphql.tools.GraphQLResolver
import ju.ma.auth.Auth0Service
import ju.ma.graphql.PaginationInput
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated

@Component
@PreAuthorize("hasAnyAuthority('read:users')")
class UserQueries(
    private val users: Auth0Service
) : GraphQLQueryResolver {
    fun users(page: PaginationInput, filter: String = ""): UsersPage {
        val userFilter = page.userFilter()
        if (filter.isNotBlank()) userFilter.withQuery(filter)
        return users.findAllUsers(userFilter)
    }

    fun user(id: String): User {
        return users.findById(id)
    }

    fun roles(page: PaginationInput, filter: String = ""): RolesPage {
        val roleFilter = RolesFilter()
            .withPage(page.page, page.size)
            .withTotals(true)
        if (filter.isNotBlank()) roleFilter.withName(filter)
        return users.findRoles(roleFilter)
    }

    fun role(id: String): Role {
        return users.findRoleById(id)
    }
}

@Component
@Validated
@PreAuthorize("hasAnyAuthority('write:users')")
class UserMutations(
    private val users: Auth0Service
) : GraphQLMutationResolver {
    fun assignRoles(roleId: String, userIds: List<String>): Role {
        return users.assignRoles(roleId, userIds)
    }

    fun removeRoles(userId: String, roleIds: List<String>): User {
        return users.removeRoles(userId, roleIds)
    }

    fun deleteUser(userId: String): Boolean {
        users.deleteById(userId)
        return true
    }

    fun removePermissions(userId: String, permissions: List<PermissionInput>): User {
        users.mgmt.users().removePermissions(userId, permissions.map { it.permission }).execute()
        return users.findById(userId)
    }
}

data class PermissionInput(
    val name: String,
    val description: String,
    val resourceServerId: String,
    val resourceServerName: String
) {
    val permission by lazy {
        val permission = com.auth0.json.mgmt.Permission()
        permission.name = name
        permission.description = description
        permission.resourceServerId = resourceServerId
        permission.resourceServerName = resourceServerName
        permission
    }
}

@Component
class RoleResolver(
    private val users: Auth0Service
) : GraphQLResolver<Role> {
    fun permissions(role: Role, page: PaginationInput?): PermissionsPage {
        val filter = if (page == null) {
            PageFilter()
        } else {
            PageFilter().withPage(page.page, page.size).withTotals(true)
        }
        return users.mgmt.roles().listPermissions(role.id, filter).execute()
    }

    fun users(role: Role, page: PaginationInput?): UsersPage {
        val filter = if (page == null) {
            PageFilter()
        } else {
            PageFilter().withPage(page.page, page.size).withTotals(true)
        }
        return users.mgmt.roles().listUsers(role.id, filter).execute()
    }
}

@Component
class UserResolver(
    private val users: Auth0Service
) : GraphQLResolver<User> {
    fun roles(user: User, page: PaginationInput?): RolesPage {
        val filter = if (page == null) {
            PageFilter()
        } else {
            PageFilter().withPage(page.page, page.size).withTotals(true)
        }
        return users.mgmt.users().listRoles(user.id, filter).execute()
    }

    fun permissions(user: User, page: PaginationInput?): PermissionsPage {
        val filter = if (page == null) {
            PageFilter()
        } else {
            PageFilter().withPage(page.page, page.size).withTotals(true)
        }
        return users.mgmt.users().listPermissions(user.id, filter).execute()
    }
}

fun PaginationInput.userFilter(): UserFilter {
    var filter = UserFilter().withPage(page, size).withTotals(true)
    var sorted = sort?.map { "${it.key}:${it.value}" }?.joinToString(" ") ?: ""
    if (sorted.isNotBlank()) filter.withSort(sorted)
    return filter
}
