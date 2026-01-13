package bsm.insert.aiassignment.domain.analytics.presentation

import bsm.insert.aiassignment.domain.analytics.application.dto.ActivityStatsResponse
import bsm.insert.aiassignment.domain.analytics.application.service.AnalyticsService
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/analytics")
class AnalyticsController(
    private val analyticsService: AnalyticsService
) {

    @GetMapping("/activity")
    fun getActivityStats(): ResponseEntity<ActivityStatsResponse> {
        val response = analyticsService.getActivityStats()
        return ResponseEntity.ok(response)
    }

    @GetMapping("/report")
    fun generateReport(): ResponseEntity<ByteArray> {
        val csvContent = analyticsService.generateChatReport()

        val headers = HttpHeaders()
        headers.contentType = MediaType.parseMediaType("text/csv")
        headers.setContentDispositionFormData("attachment", "chat_report.csv")

        return ResponseEntity.ok()
            .headers(headers)
            .body(csvContent.toByteArray(Charsets.UTF_8))
    }
}