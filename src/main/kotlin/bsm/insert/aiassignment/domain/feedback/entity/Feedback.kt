package bsm.insert.aiassignment.domain.feedback.entity

import bsm.insert.aiassignment.domain.chat.entity.Chat
import bsm.insert.aiassignment.domain.user.entity.User
import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(
    name = "feedbacks",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["user_id", "chat_id"])
    ]
)
class Feedback(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    val chat: Chat,

    @Column(name = "is_positive", nullable = false)
    val isPositive: Boolean,

    @Column(name = "created_at", nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: FeedbackStatus = FeedbackStatus.PENDING
)
