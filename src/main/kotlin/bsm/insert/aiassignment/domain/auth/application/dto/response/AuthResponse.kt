package bsm.insert.aiassignment.domain.auth.application.dto.response

import bsm.insert.aiassignment.domain.user.domain.type.Role
import bsm.insert.aiassignment.domain.user.domain.User
import java.time.OffsetDateTime

data class UserResponse(
    val id: Long,
    val email: String,
    val name: String,
    val role: Role,
    val createdAt: OffsetDateTime
) {
    companion object {
        fun from(user: User): UserResponse {
            return UserResponse(
                id = user.id,
                email = user.email,
                name = user.name,
                role = user.role,
                createdAt = user.createdAt
            )
        }
    }
}
