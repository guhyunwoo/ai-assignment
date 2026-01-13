package bsm.insert.aiassignment.domain.analytics.domain

import bsm.insert.aiassignment.domain.analytics.domain.type.ActivityType
import bsm.insert.aiassignment.domain.user.domain.User
import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "activity_logs")
class ActivityLog(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    val user: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val activityType: ActivityType,

    @Column(nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now()
)
