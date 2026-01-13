package bsm.insert.aiassignment.domain.analytics.repository

import bsm.insert.aiassignment.domain.analytics.entity.ActivityLog
import bsm.insert.aiassignment.domain.analytics.entity.ActivityType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime
import java.util.*

@Repository
interface ActivityLogRepository : JpaRepository<ActivityLog, UUID> {

    @Query("SELECT COUNT(a) FROM ActivityLog a WHERE a.activityType = :activityType AND a.createdAt >= :startTime AND a.createdAt < :endTime")
    fun countByActivityTypeAndCreatedAtBetween(
        activityType: ActivityType,
        startTime: OffsetDateTime,
        endTime: OffsetDateTime
    ): Long
}
