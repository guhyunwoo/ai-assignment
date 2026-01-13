package bsm.insert.aiassignment.domain.feedback.domain

import bsm.insert.aiassignment.domain.chat.domain.Chat
import bsm.insert.aiassignment.domain.feedback.domain.type.FeedbackStatus
import bsm.insert.aiassignment.domain.user.domain.User
import jakarta.persistence.*
import java.time.OffsetDateTime

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
    val id: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    val chat: Chat,

    @Column(nullable = false)
    val isPositive: Boolean,

    @Column(nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: FeedbackStatus = FeedbackStatus.PENDING
)
