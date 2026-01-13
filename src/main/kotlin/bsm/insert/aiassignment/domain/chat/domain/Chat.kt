package bsm.insert.aiassignment.domain.chat.domain

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "chats")
class Chat(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: Long,

    @Column(nullable = false, columnDefinition = "TEXT")
    val question: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    var answer: String = "",

    @Column(nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    val thread: Thread
)
