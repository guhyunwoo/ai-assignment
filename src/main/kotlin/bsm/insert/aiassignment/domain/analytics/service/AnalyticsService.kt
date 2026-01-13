package bsm.insert.aiassignment.domain.analytics.service

import bsm.insert.aiassignment.domain.analytics.dto.ActivityStatsResponse
import bsm.insert.aiassignment.domain.analytics.dto.ChatReportRow
import bsm.insert.aiassignment.domain.analytics.entity.ActivityLog
import bsm.insert.aiassignment.domain.analytics.entity.ActivityType
import bsm.insert.aiassignment.domain.analytics.repository.ActivityLogRepository
import bsm.insert.aiassignment.domain.chat.repository.ChatRepository
import bsm.insert.aiassignment.domain.user.entity.User
import bsm.insert.aiassignment.global.exception.AccessDeniedException
import bsm.insert.aiassignment.global.security.UserPrincipal
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.StringWriter
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Service
@Transactional(readOnly = true)
class AnalyticsService(
    private val activityLogRepository: ActivityLogRepository,
    private val chatRepository: ChatRepository
) {

    @Transactional
    fun logActivity(user: User, activityType: ActivityType) {
        val activityLog = ActivityLog(
            user = user,
            activityType = activityType
        )
        activityLogRepository.save(activityLog)
    }

    fun getActivityStats(principal: UserPrincipal): ActivityStatsResponse {
        if (!principal.isAdmin()) {
            throw AccessDeniedException()
        }

        val now = OffsetDateTime.now()
        val startOfDay = now.toLocalDate().atStartOfDay().atOffset(ZoneOffset.UTC)
        val endOfDay = startOfDay.plusDays(1)

        val signupCount = activityLogRepository.countByActivityTypeAndCreatedAtBetween(
            ActivityType.SIGNUP,
            startOfDay,
            endOfDay
        )

        val loginCount = activityLogRepository.countByActivityTypeAndCreatedAtBetween(
            ActivityType.LOGIN,
            startOfDay,
            endOfDay
        )

        val chatCount = chatRepository.countByCreatedAtBetween(startOfDay, endOfDay)

        return ActivityStatsResponse(
            signupCount = signupCount,
            loginCount = loginCount,
            chatCount = chatCount,
            periodStart = startOfDay,
            periodEnd = endOfDay
        )
    }

    fun generateChatReport(principal: UserPrincipal): String {
        if (!principal.isAdmin()) {
            throw AccessDeniedException()
        }

        val now = OffsetDateTime.now()
        val startOfDay = now.toLocalDate().atStartOfDay().atOffset(ZoneOffset.UTC)
        val endOfDay = startOfDay.plusDays(1)

        val chats = chatRepository.findByCreatedAtBetween(startOfDay, endOfDay)

        val reportRows = chats.map { chat ->
            ChatReportRow(
                chatId = chat.id.toString(),
                question = chat.question,
                answer = chat.answer,
                createdAt = chat.createdAt,
                userId = chat.thread.user.id.toString(),
                userEmail = chat.thread.user.email,
                userName = chat.thread.user.name
            )
        }

        return generateCsv(reportRows)
    }

    private fun generateCsv(rows: List<ChatReportRow>): String {
        val writer = StringWriter()

        writer.append("chat_id,question,answer,created_at,user_id,user_email,user_name\n")

        rows.forEach { row ->
            writer.append(escapeCsv(row.chatId))
            writer.append(",")
            writer.append(escapeCsv(row.question))
            writer.append(",")
            writer.append(escapeCsv(row.answer))
            writer.append(",")
            writer.append(escapeCsv(row.createdAt.toString()))
            writer.append(",")
            writer.append(escapeCsv(row.userId))
            writer.append(",")
            writer.append(escapeCsv(row.userEmail))
            writer.append(",")
            writer.append(escapeCsv(row.userName))
            writer.append("\n")
        }

        return writer.toString()
    }

    private fun escapeCsv(value: String): String {
        val needsQuoting = value.contains(",") || value.contains("\"") || value.contains("\n")
        return if (needsQuoting) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
}
