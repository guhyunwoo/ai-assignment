package bsm.insert.aiassignment.domain.analytics.controller

import bsm.insert.aiassignment.domain.analytics.dto.ActivityStatsResponse
import bsm.insert.aiassignment.domain.analytics.service.AnalyticsService
import bsm.insert.aiassignment.global.security.UserPrincipal
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/analytics")
class AnalyticsController(
    private val analyticsService: AnalyticsService
) {

    @GetMapping("/activity")
    fun getActivityStats(
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ActivityStatsResponse> {
        val response = analyticsService.getActivityStats(principal)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/report")
    fun generateReport(
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ByteArray> {
        val csvContent = analyticsService.generateChatReport(principal)

        val headers = HttpHeaders()
        headers.contentType = MediaType.parseMediaType("text/csv")
        headers.setContentDispositionFormData("attachment", "chat_report.csv")

        return ResponseEntity.ok()
            .headers(headers)
            .body(csvContent.toByteArray(Charsets.UTF_8))
    }
}
