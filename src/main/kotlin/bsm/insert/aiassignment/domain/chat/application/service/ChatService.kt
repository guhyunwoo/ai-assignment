package bsm.insert.aiassignment.domain.chat.application.service

import bsm.insert.aiassignment.domain.chat.application.dto.ChatRequest
import bsm.insert.aiassignment.domain.chat.application.dto.ChatResponse
import bsm.insert.aiassignment.domain.chat.application.dto.ThreadListResponse
import bsm.insert.aiassignment.domain.chat.application.dto.ThreadResponse
import bsm.insert.aiassignment.domain.chat.application.exception.ThreadNotFoundException
import bsm.insert.aiassignment.domain.chat.domain.Chat
import bsm.insert.aiassignment.domain.chat.domain.Thread
import bsm.insert.aiassignment.domain.chat.repository.ChatRepository
import bsm.insert.aiassignment.domain.chat.repository.ThreadRepository
import bsm.insert.aiassignment.domain.common.snowflake.Snowflake
import bsm.insert.aiassignment.domain.user.application.exception.UserNotFoundException
import bsm.insert.aiassignment.domain.user.repository.UserRepository
import bsm.insert.aiassignment.global.exception.CantAccessException
import bsm.insert.aiassignment.global.openai.OpenAiService
import bsm.insert.aiassignment.global.security.auth.AuthUserDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Service
class ChatService(
    private val threadRepository: ThreadRepository,
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val openAiService: OpenAiService,
    private val snowflake: Snowflake
) {

    companion object {
        private const val THREAD_TIMEOUT_MINUTES = 30L
    }

    @Transactional
    fun createChat(request: ChatRequest, authUserDetails: AuthUserDetails): ChatResponse {
        val thread = getOrCreateThread(authUserDetails.username.toLong())

        val history = chatRepository.findByThreadIdOrderByCreatedAtAsc(thread.id)

        val answer = openAiService.generateResponse(request.question, history, request.model)

        val chat = Chat(
            question = request.question,
            answer = answer,
            thread = thread,
            id = snowflake.nextId()
        )

        val savedChat = chatRepository.save(chat)
        return ChatResponse.from(savedChat)
    }

    @Transactional
    fun createStreamingChat(request: ChatRequest, authUserDetails: AuthUserDetails): Pair<Long, Flow<String>> {
        val thread = getOrCreateThread(authUserDetails.username.toLong())

        val history = chatRepository.findByThreadIdOrderByCreatedAtAsc(thread.id)

        val chat = Chat(
            question = request.question,
            answer = "",
            thread = thread,
            id = snowflake.nextId()
        )
        val savedChat = chatRepository.save(chat)

        val answerBuilder = StringBuilder()
        val flow = openAiService.generateStreamingResponse(request.question, history, request.model)
            .onEach { chunk -> answerBuilder.append(chunk) }
            .onCompletion {
                savedChat.answer = answerBuilder.toString()
                withContext(Dispatchers.IO) {
                    chatRepository.save(savedChat)
                }
            }

        return Pair(savedChat.id, flow)
    }

    fun getThreads(
        authUserDetails: AuthUserDetails,
        page: Int,
        size: Int,
        sortDirection: String
    ): ThreadListResponse {
        val sort = if (sortDirection.equals("asc", ignoreCase = true)) {
            Sort.by("createdAt").ascending()
        } else {
            Sort.by("createdAt").descending()
        }

        val pageable = PageRequest.of(page, size, sort)

        val threadPage = if (authUserDetails.isAdmin()) {
            threadRepository.findAllBy(pageable)
        } else {
            threadRepository.findByUserId(authUserDetails.username.toLong(), pageable)
        }

        return ThreadListResponse(
            threads = threadPage.content.map { ThreadResponse.from(it) },
            page = page,
            size = size,
            totalElements = threadPage.totalElements,
            totalPages = threadPage.totalPages
        )
    }

    @Transactional
    fun deleteThread(threadId: Long, authUserDetails: AuthUserDetails) {
        val thread = threadRepository.findById(threadId)
            .orElseThrow { ThreadNotFoundException() }

        if (!authUserDetails.isAdmin() && thread.user.id != authUserDetails.username.toLong()) {
            throw CantAccessException()
        }

        threadRepository.delete(thread)
    }

    private fun getOrCreateThread(userId: Long): Thread {
        val latestThreads = threadRepository.findLatestByUserId(
            userId,
            PageRequest.of(0, 1)
        )

        val latestThread = latestThreads.firstOrNull()

        if (latestThread != null) {
            val lastChat = latestThread.chats.maxByOrNull { it.createdAt }
            val timeoutThreshold = OffsetDateTime.now().minusMinutes(THREAD_TIMEOUT_MINUTES)

            if (lastChat != null && lastChat.createdAt.isAfter(timeoutThreshold)) {
                return latestThread
            }

            if (latestThread.chats.isEmpty() && latestThread.createdAt.isAfter(timeoutThreshold)) {
                return latestThread
            }
        }

        val user = userRepository.findById(userId)
            .orElseThrow { UserNotFoundException() }
        val newThread = Thread(
            user = user,
            id = snowflake.nextId()
        )
        return threadRepository.save(newThread)
    }
}
