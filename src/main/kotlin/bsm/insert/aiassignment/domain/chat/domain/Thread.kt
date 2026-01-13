package bsm.insert.aiassignment.domain.chat.domain

import bsm.insert.aiassignment.domain.user.domain.User
import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "threads")
class Thread(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    val user: User,

    @Column(nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @OneToMany(mappedBy = "thread", cascade = [CascadeType.ALL], orphanRemoval = true)
    val chats: MutableList<Chat> = mutableListOf()
)
