package bsm.insert.aiassignment.domain.feedback.application.service

import bsm.insert.aiassignment.domain.chat.repository.ChatRepository
import bsm.insert.aiassignment.domain.common.snowflake.Snowflake
import bsm.insert.aiassignment.domain.feedback.application.dto.FeedbackCreateRequest
import bsm.insert.aiassignment.domain.feedback.application.dto.FeedbackListResponse
import bsm.insert.aiassignment.domain.feedback.application.dto.FeedbackResponse
import bsm.insert.aiassignment.domain.feedback.application.dto.FeedbackStatusUpdateRequest
import bsm.insert.aiassignment.domain.feedback.application.exception.ChatNotFoundException
import bsm.insert.aiassignment.domain.feedback.application.exception.DuplicateFeedbackException
import bsm.insert.aiassignment.domain.feedback.application.exception.FeedbackNotFoundException
import bsm.insert.aiassignment.domain.feedback.domain.Feedback
import bsm.insert.aiassignment.domain.feedback.repository.FeedbackRepository
import bsm.insert.aiassignment.domain.user.application.exception.UserNotFoundException
import bsm.insert.aiassignment.domain.user.repository.UserRepository
import bsm.insert.aiassignment.global.exception.CantAccessException
import bsm.insert.aiassignment.global.security.auth.AuthUserDetails
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FeedbackService(
    private val feedbackRepository: FeedbackRepository,
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val snowflake: Snowflake,
) {

    @Transactional
    fun createFeedback(request: FeedbackCreateRequest, authUserDetails: AuthUserDetails): FeedbackResponse {
        val userId = authUserDetails.username.toLong()

        val user = userRepository.findById(userId)
            .orElseThrow { UserNotFoundException() }

        val chat = chatRepository.findById(request.chatId)
            .orElseThrow { ChatNotFoundException() }

        if (!authUserDetails.isAdmin() && chat.thread.user.id != userId) {
            throw CantAccessException()
        }

        if (feedbackRepository.existsByUserIdAndChatId(userId, request.chatId)) {
            throw DuplicateFeedbackException()
        }

        val feedback = Feedback(
            id = snowflake.nextId(),
            user = user,
            chat = chat,
            isPositive = request.isPositive,
        )

        val savedFeedback = feedbackRepository.save(feedback)
        return FeedbackResponse.from(savedFeedback)
    }

    fun getFeedbacks(
        authUserDetails: AuthUserDetails,
        page: Int,
        size: Int,
        sortDirection: String,
        isPositive: Boolean?
    ): FeedbackListResponse {
        val sort = if (sortDirection.equals("asc", ignoreCase = true)) {
            Sort.by("createdAt").ascending()
        } else {
            Sort.by("createdAt").descending()
        }

        val pageable = PageRequest.of(page, size, sort)

        val userId = authUserDetails.username.toLong()
        val isAdmin = authUserDetails.isAdmin()

        val feedbackPage = when {
            isAdmin && isPositive != null -> {
                feedbackRepository.findByIsPositive(isPositive, pageable)
            }
            isAdmin -> {
                feedbackRepository.findAllBy(pageable)
            }
            isPositive != null -> {
                feedbackRepository.findByUserIdAndIsPositive(userId, isPositive, pageable)
            }
            else -> {
                feedbackRepository.findByUserId(userId, pageable)
            }
        }

        return FeedbackListResponse(
            feedbacks = feedbackPage.content.map { FeedbackResponse.from(it) },
            page = page,
            size = size,
            totalElements = feedbackPage.totalElements,
            totalPages = feedbackPage.totalPages
        )
    }

    @Transactional
    fun updateFeedbackStatus(
        feedbackId: Long,
        request: FeedbackStatusUpdateRequest,
        authUserDetails: AuthUserDetails,
    ): FeedbackResponse {
        if (!authUserDetails.isAdmin()) {
            throw CantAccessException()
        }

        val feedback = feedbackRepository.findById(feedbackId)
            .orElseThrow { FeedbackNotFoundException() }

        feedback.status = request.status
        val savedFeedback = feedbackRepository.save(feedback)
        return FeedbackResponse.from(savedFeedback)
    }
}
