package bsm.insert.aiassignment.domain.analytics.application.dto

import java.time.OffsetDateTime

data class ActivityStatsResponse(
    val signupCount: Long,
    val loginCount: Long,
    val chatCount: Long,
    val periodStart: OffsetDateTime,
    val periodEnd: OffsetDateTime
)

data class ChatReportRow(
    val chatId: String,
    val question: String,
    val answer: String,
    val createdAt: OffsetDateTime,
    val userId: String,
    val userEmail: String,
    val userName: String
)
