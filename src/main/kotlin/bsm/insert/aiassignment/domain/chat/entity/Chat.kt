package bsm.insert.aiassignment.domain.chat.entity

import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(name = "chats")
class Chat(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(nullable = false, columnDefinition = "TEXT")
    val question: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    var answer: String = "",

    @Column(name = "created_at", nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thread_id", nullable = false)
    val thread: Thread
)
