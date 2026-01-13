package bsm.insert.aiassignment.domain.feedback.presentation

import bsm.insert.aiassignment.domain.feedback.application.dto.FeedbackCreateRequest
import bsm.insert.aiassignment.domain.feedback.application.dto.FeedbackListResponse
import bsm.insert.aiassignment.domain.feedback.application.dto.FeedbackResponse
import bsm.insert.aiassignment.domain.feedback.application.dto.FeedbackStatusUpdateRequest
import bsm.insert.aiassignment.domain.feedback.application.service.FeedbackService
import bsm.insert.aiassignment.global.security.auth.AuthUserDetails
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/feedbacks")
class FeedbackController(
    private val feedbackService: FeedbackService
) {
    @PostMapping
    fun createFeedback(
        @Valid @RequestBody request: FeedbackCreateRequest,
        @AuthenticationPrincipal authUserDetails: AuthUserDetails
    ): ResponseEntity<FeedbackResponse> {
        val response = feedbackService.createFeedback(request, authUserDetails)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    fun getFeedbacks(
        @AuthenticationPrincipal authUserDetails: AuthUserDetails,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "desc") sort: String,
        @RequestParam(required = false) isPositive: Boolean?
    ): ResponseEntity<FeedbackListResponse> {
        val response = feedbackService.getFeedbacks(authUserDetails, page, size, sort, isPositive)
        return ResponseEntity.ok(response)
    }

    @PatchMapping("/{feedbackId}/status")
    fun updateFeedbackStatus(
        @PathVariable feedbackId: Long,
        @Valid @RequestBody request: FeedbackStatusUpdateRequest,
        @AuthenticationPrincipal authUserDetails: AuthUserDetails
    ): ResponseEntity<FeedbackResponse> {
        val response = feedbackService.updateFeedbackStatus(feedbackId, request, authUserDetails)
        return ResponseEntity.ok(response)
    }
}