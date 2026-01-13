package bsm.insert.aiassignment.domain.feedback.service

import bsm.insert.aiassignment.domain.chat.repository.ChatRepository
import bsm.insert.aiassignment.domain.feedback.dto.*
import bsm.insert.aiassignment.domain.feedback.entity.Feedback
import bsm.insert.aiassignment.domain.feedback.repository.FeedbackRepository
import bsm.insert.aiassignment.domain.user.repository.UserRepository
import bsm.insert.aiassignment.global.exception.*
import bsm.insert.aiassignment.global.security.UserPrincipal
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional(readOnly = true)
class FeedbackService(
    private val feedbackRepository: FeedbackRepository,
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository
) {

    @Transactional
    fun createFeedback(request: FeedbackCreateRequest, principal: UserPrincipal): FeedbackResponse {
        val user = userRepository.findById(principal.id)
            .orElseThrow { UserNotFoundException() }

        val chat = chatRepository.findById(request.chatId)
            .orElseThrow { ChatNotFoundException() }

        if (!principal.isAdmin() && chat.thread.user.id != principal.id) {
            throw AccessDeniedException()
        }

        if (feedbackRepository.existsByUserIdAndChatId(principal.id, request.chatId)) {
            throw DuplicateFeedbackException()
        }

        val feedback = Feedback(
            user = user,
            chat = chat,
            isPositive = request.isPositive
        )

        val savedFeedback = feedbackRepository.save(feedback)
        return FeedbackResponse.from(savedFeedback)
    }

    fun getFeedbacks(
        principal: UserPrincipal,
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

        val feedbackPage = when {
            principal.isAdmin() && isPositive != null -> {
                feedbackRepository.findByIsPositive(isPositive, pageable)
            }
            principal.isAdmin() -> {
                feedbackRepository.findAllBy(pageable)
            }
            isPositive != null -> {
                feedbackRepository.findByUserIdAndIsPositive(principal.id, isPositive, pageable)
            }
            else -> {
                feedbackRepository.findByUserId(principal.id, pageable)
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
        feedbackId: UUID,
        request: FeedbackStatusUpdateRequest,
        principal: UserPrincipal
    ): FeedbackResponse {
        if (!principal.isAdmin()) {
            throw AccessDeniedException()
        }

        val feedback = feedbackRepository.findById(feedbackId)
            .orElseThrow { FeedbackNotFoundException() }

        feedback.status = request.status
        val savedFeedback = feedbackRepository.save(feedback)
        return FeedbackResponse.from(savedFeedback)
    }
}
