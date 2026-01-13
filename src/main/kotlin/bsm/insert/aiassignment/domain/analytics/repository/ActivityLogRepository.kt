package bsm.insert.aiassignment.domain.analytics.repository

import bsm.insert.aiassignment.domain.analytics.domain.ActivityLog
import bsm.insert.aiassignment.domain.analytics.domain.type.ActivityType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

@Repository
interface ActivityLogRepository : JpaRepository<ActivityLog, Long> {

    @Query("SELECT COUNT(a) FROM ActivityLog a WHERE a.activityType = :activityType AND a.createdAt >= :startTime AND a.createdAt < :endTime")
    fun countByActivityTypeAndCreatedAtBetween(
        activityType: ActivityType,
        startTime: OffsetDateTime,
        endTime: OffsetDateTime
    ): Long
}
