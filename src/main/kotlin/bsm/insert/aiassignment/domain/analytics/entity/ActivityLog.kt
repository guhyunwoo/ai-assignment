package bsm.insert.aiassignment.domain.analytics.entity

import bsm.insert.aiassignment.domain.user.entity.User
import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(name = "activity_logs")
class ActivityLog(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false)
    val activityType: ActivityType,

    @Column(name = "created_at", nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now()
)
