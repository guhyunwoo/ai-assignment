package bsm.insert.aiassignment.domain.user.repository

import bsm.insert.aiassignment.domain.user.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): User?
    fun existsByEmail(email: String): Boolean
}
