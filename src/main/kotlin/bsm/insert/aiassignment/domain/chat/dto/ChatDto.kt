package bsm.insert.aiassignment.domain.chat.dto

import bsm.insert.aiassignment.domain.chat.entity.Chat
import bsm.insert.aiassignment.domain.chat.entity.Thread
import jakarta.validation.constraints.NotBlank
import java.time.OffsetDateTime
import java.util.*

data class ChatRequest(
    @field:NotBlank(message = "질문은 필수입니다")
    val question: String,

    val isStreaming: Boolean = false,

    val model: String? = null
)

data class ChatResponse(
    val id: UUID,
    val question: String,
    val answer: String,
    val createdAt: OffsetDateTime,
    val threadId: UUID
) {
    companion object {
        fun from(chat: Chat): ChatResponse {
            return ChatResponse(
                id = chat.id!!,
                question = chat.question,
                answer = chat.answer,
                createdAt = chat.createdAt,
                threadId = chat.thread.id!!
            )
        }
    }
}

data class ThreadResponse(
    val id: UUID,
    val createdAt: OffsetDateTime,
    val chats: List<ChatResponse>
) {
    companion object {
        fun from(thread: Thread): ThreadResponse {
            return ThreadResponse(
                id = thread.id!!,
                createdAt = thread.createdAt,
                chats = thread.chats.map { ChatResponse.from(it) }
            )
        }
    }
}

data class ThreadListResponse(
    val threads: List<ThreadResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int
)
