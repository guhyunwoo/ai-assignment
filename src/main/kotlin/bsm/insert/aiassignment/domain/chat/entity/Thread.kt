package bsm.insert.aiassignment.domain.chat.entity

import bsm.insert.aiassignment.domain.user.entity.User
import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(name = "threads")
class Thread(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(name = "created_at", nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @OneToMany(mappedBy = "thread", cascade = [CascadeType.ALL], orphanRemoval = true)
    val chats: MutableList<Chat> = mutableListOf()
)
