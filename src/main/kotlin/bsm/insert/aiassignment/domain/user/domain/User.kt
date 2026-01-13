package bsm.insert.aiassignment.domain.user.domain

import bsm.insert.aiassignment.domain.user.domain.type.Role
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime

@Entity
@Table(name = "users")
class User(
    @Id
    val id: Long,

    @Column(nullable = false, unique = true)
    val email: String,

    @Column(nullable = false)
    var password: String,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: Role = Role.MEMBER
)
