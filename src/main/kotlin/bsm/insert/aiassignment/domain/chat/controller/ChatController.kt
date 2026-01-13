package bsm.insert.aiassignment.domain.chat.controller

import bsm.insert.aiassignment.domain.chat.dto.*
import bsm.insert.aiassignment.domain.chat.service.ChatService
import bsm.insert.aiassignment.global.security.UserPrincipal
import jakarta.validation.Valid
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.*
import java.util.concurrent.Executors

@RestController
@RequestMapping("/api/chats")
class ChatController(
    private val chatService: ChatService
) {
    private val executor = Executors.newCachedThreadPool()

    @PostMapping
    fun createChat(
        @Valid @RequestBody request: ChatRequest,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<Any> {
        return if (request.isStreaming) {
            val emitter = SseEmitter(60000L)

            executor.execute {
                try {
                    val (chatId, flow) = chatService.createStreamingChat(request, principal)

                    emitter.send(
                        SseEmitter.event()
                            .name("start")
                            .data(mapOf("chatId" to chatId.toString()))
                    )

                    runBlocking {
                        flow.toList().forEach { chunk ->
                            if (chunk.isNotEmpty()) {
                                emitter.send(
                                    SseEmitter.event()
                                        .name("message")
                                        .data(mapOf("content" to chunk))
                                )
                            }
                        }
                    }

                    emitter.send(SseEmitter.event().name("done").data(""))
                    emitter.complete()
                } catch (e: Exception) {
                    emitter.completeWithError(e)
                }
            }

            ResponseEntity.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(emitter)
        } else {
            val response = chatService.createChat(request, principal)
            ResponseEntity.status(HttpStatus.CREATED).body(response)
        }
    }

    @GetMapping("/threads")
    fun getThreads(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "desc") sort: String
    ): ResponseEntity<ThreadListResponse> {
        val response = chatService.getThreads(principal, page, size, sort)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/threads/{threadId}")
    fun deleteThread(
        @PathVariable threadId: UUID,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<Unit> {
        chatService.deleteThread(threadId, principal)
        return ResponseEntity.noContent().build()
    }
}
