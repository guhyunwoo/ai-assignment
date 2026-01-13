package bsm.insert.aiassignment.domain.chat.service

import bsm.insert.aiassignment.domain.chat.dto.*
import bsm.insert.aiassignment.domain.chat.entity.Chat
import bsm.insert.aiassignment.domain.chat.entity.Thread
import bsm.insert.aiassignment.domain.chat.repository.ChatRepository
import bsm.insert.aiassignment.domain.chat.repository.ThreadRepository
import bsm.insert.aiassignment.domain.user.entity.Role
import bsm.insert.aiassignment.domain.user.repository.UserRepository
import bsm.insert.aiassignment.global.exception.AccessDeniedException
import bsm.insert.aiassignment.global.exception.ThreadNotFoundException
import bsm.insert.aiassignment.global.exception.UserNotFoundException
import bsm.insert.aiassignment.global.openai.OpenAiService
import bsm.insert.aiassignment.global.security.UserPrincipal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.*

@Service
@Transactional(readOnly = true)
class ChatService(
    private val threadRepository: ThreadRepository,
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val openAiService: OpenAiService
) {

    companion object {
        private const val THREAD_TIMEOUT_MINUTES = 30L
    }

    @Transactional
    fun createChat(request: ChatRequest, principal: UserPrincipal): ChatResponse {
        val user = userRepository.findById(principal.id)
            .orElseThrow { UserNotFoundException() }

        val thread = getOrCreateThread(principal.id)

        val history = chatRepository.findByThreadIdOrderByCreatedAtAsc(thread.id!!)

        val answer = openAiService.generateResponse(request.question, history, request.model)

        val chat = Chat(
            question = request.question,
            answer = answer,
            thread = thread
        )

        val savedChat = chatRepository.save(chat)
        return ChatResponse.from(savedChat)
    }

    @Transactional
    fun createStreamingChat(request: ChatRequest, principal: UserPrincipal): Pair<UUID, Flow<String>> {
        val user = userRepository.findById(principal.id)
            .orElseThrow { UserNotFoundException() }

        val thread = getOrCreateThread(principal.id)

        val history = chatRepository.findByThreadIdOrderByCreatedAtAsc(thread.id!!)

        val chat = Chat(
            question = request.question,
            answer = "",
            thread = thread
        )
        val savedChat = chatRepository.save(chat)

        val answerBuilder = StringBuilder()
        val flow = openAiService.generateStreamingResponse(request.question, history, request.model)
            .onEach { chunk -> answerBuilder.append(chunk) }
            .onCompletion {
                savedChat.answer = answerBuilder.toString()
                chatRepository.save(savedChat)
            }

        return Pair(savedChat.id!!, flow)
    }

    fun getThreads(
        principal: UserPrincipal,
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

        val threadPage = if (principal.isAdmin()) {
            threadRepository.findAllBy(pageable)
        } else {
            threadRepository.findByUserId(principal.id, pageable)
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
    fun deleteThread(threadId: UUID, principal: UserPrincipal) {
        val thread = threadRepository.findById(threadId)
            .orElseThrow { ThreadNotFoundException() }

        if (!principal.isAdmin() && thread.user.id != principal.id) {
            throw AccessDeniedException()
        }

        threadRepository.delete(thread)
    }

    private fun getOrCreateThread(userId: UUID): Thread {
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

        val user = userRepository.findById(userId).orElseThrow { UserNotFoundException() }
        val newThread = Thread(user = user)
        return threadRepository.save(newThread)
    }
}
