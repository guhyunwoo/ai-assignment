package bsm.insert.aiassignment.global.security

import bsm.insert.aiassignment.domain.user.entity.Role
import java.util.*

data class UserPrincipal(
    val id: UUID,
    val email: String,
    val role: Role
) {
    fun isAdmin(): Boolean = role == Role.ADMIN
}
