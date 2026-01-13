package bsm.insert.aiassignment.domain.feedback.application.dto

import bsm.insert.aiassignment.domain.feedback.domain.Feedback
import bsm.insert.aiassignment.domain.feedback.domain.type.FeedbackStatus
import jakarta.validation.constraints.NotNull
import java.time.OffsetDateTime

data class FeedbackCreateRequest(
    @field:NotNull(message = "대화 ID는 필수입니다")
    val chatId: Long,

    @field:NotNull(message = "긍정/부정 여부는 필수입니다")
    val isPositive: Boolean
)

data class FeedbackStatusUpdateRequest(
    @field:NotNull(message = "상태는 필수입니다")
    val status: FeedbackStatus
)

data class FeedbackResponse(
    val id: Long,
    val userId: Long,
    val chatId: Long,
    val isPositive: Boolean,
    val status: FeedbackStatus,
    val createdAt: OffsetDateTime
) {
    companion object {
        fun from(feedback: Feedback): FeedbackResponse {
            return FeedbackResponse(
                id = feedback.id,
                userId = feedback.user.id,
                chatId = feedback.chat.id,
                isPositive = feedback.isPositive,
                status = feedback.status,
                createdAt = feedback.createdAt
            )
        }
    }
}

data class FeedbackListResponse(
    val feedbacks: List<FeedbackResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int
)
