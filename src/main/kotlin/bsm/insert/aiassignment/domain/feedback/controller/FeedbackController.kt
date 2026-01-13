package bsm.insert.aiassignment.domain.feedback.controller

import bsm.insert.aiassignment.domain.feedback.dto.*
import bsm.insert.aiassignment.domain.feedback.service.FeedbackService
import bsm.insert.aiassignment.global.security.UserPrincipal
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/feedbacks")
class FeedbackController(
    private val feedbackService: FeedbackService
) {

    @PostMapping
    fun createFeedback(
        @Valid @RequestBody request: FeedbackCreateRequest,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<FeedbackResponse> {
        val response = feedbackService.createFeedback(request, principal)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    fun getFeedbacks(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "desc") sort: String,
        @RequestParam(required = false) isPositive: Boolean?
    ): ResponseEntity<FeedbackListResponse> {
        val response = feedbackService.getFeedbacks(principal, page, size, sort, isPositive)
        return ResponseEntity.ok(response)
    }

    @PatchMapping("/{feedbackId}/status")
    fun updateFeedbackStatus(
        @PathVariable feedbackId: UUID,
        @Valid @RequestBody request: FeedbackStatusUpdateRequest,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<FeedbackResponse> {
        val response = feedbackService.updateFeedbackStatus(feedbackId, request, principal)
        return ResponseEntity.ok(response)
    }
}
