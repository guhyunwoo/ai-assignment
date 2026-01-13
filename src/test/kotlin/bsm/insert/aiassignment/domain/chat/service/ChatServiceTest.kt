package bsm.insert.aiassignment.domain.chat.service

import bsm.insert.aiassignment.domain.chat.dto.ChatRequest
import bsm.insert.aiassignment.domain.chat.entity.Chat
import bsm.insert.aiassignment.domain.chat.entity.Thread
import bsm.insert.aiassignment.domain.chat.repository.ChatRepository
import bsm.insert.aiassignment.domain.chat.repository.ThreadRepository
import bsm.insert.aiassignment.domain.user.entity.Role
import bsm.insert.aiassignment.domain.user.entity.User
import bsm.insert.aiassignment.domain.user.repository.UserRepository
import bsm.insert.aiassignment.global.exception.AccessDeniedException
import bsm.insert.aiassignment.global.exception.ThreadNotFoundException
import bsm.insert.aiassignment.global.openai.OpenAiService
import bsm.insert.aiassignment.global.security.UserPrincipal
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.OffsetDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class ChatServiceTest {

    @Mock
    private lateinit var threadRepository: ThreadRepository

    @Mock
    private lateinit var chatRepository: ChatRepository

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var openAiService: OpenAiService

    @InjectMocks
    private lateinit var chatService: ChatService

    private lateinit var testUser: User
    private lateinit var testThread: Thread
    private lateinit var testChat: Chat
    private lateinit var principal: UserPrincipal
    private val testUserId = UUID.randomUUID()
    private val testThreadId = UUID.randomUUID()
    private val testChatId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        testUser = User(
            id = testUserId,
            email = "test@example.com",
            password = "password",
            name = "Test User",
            role = Role.MEMBER
        )

        testThread = Thread(
            id = testThreadId,
            user = testUser,
            createdAt = OffsetDateTime.now()
        )

        testChat = Chat(
            id = testChatId,
            question = "Hello",
            answer = "Hi there!",
            thread = testThread
        )

        principal = UserPrincipal(
            id = testUserId,
            email = "test@example.com",
            role = Role.MEMBER
        )
    }

    @Test
    @DisplayName("대화 생성 성공 - 새 스레드 생성")
    fun createChat_NewThread_Success() {
        // given
        val request = ChatRequest(question = "Hello", isStreaming = false)

        whenever(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser))
        whenever(threadRepository.findLatestByUserId(eq(testUserId), any())).thenReturn(emptyList())
        whenever(threadRepository.save(any<Thread>())).thenReturn(testThread)
        whenever(chatRepository.findByThreadIdOrderByCreatedAtAsc(testThreadId)).thenReturn(emptyList())
        whenever(openAiService.generateResponse(any(), any(), anyOrNull())).thenReturn("Hi there!")
        whenever(chatRepository.save(any<Chat>())).thenReturn(testChat)

        // when
        val result = chatService.createChat(request, principal)

        // then
        assertNotNull(result)
        assertEquals("Hello", result.question)
        assertEquals("Hi there!", result.answer)
        verify(threadRepository).save(any<Thread>())
    }

    @Test
    @DisplayName("대화 생성 성공 - 기존 스레드 유지 (30분 이내)")
    fun createChat_ExistingThread_Success() {
        // given
        val request = ChatRequest(question = "Hello", isStreaming = false)
        val recentChat = Chat(
            id = UUID.randomUUID(),
            question = "Previous",
            answer = "Previous answer",
            thread = testThread,
            createdAt = OffsetDateTime.now().minusMinutes(10)
        )
        testThread.chats.add(recentChat)

        whenever(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser))
        whenever(threadRepository.findLatestByUserId(eq(testUserId), any())).thenReturn(listOf(testThread))
        whenever(chatRepository.findByThreadIdOrderByCreatedAtAsc(testThreadId)).thenReturn(listOf(recentChat))
        whenever(openAiService.generateResponse(any(), any(), anyOrNull())).thenReturn("Hi there!")
        whenever(chatRepository.save(any<Chat>())).thenReturn(testChat)

        // when
        val result = chatService.createChat(request, principal)

        // then
        assertNotNull(result)
        verify(threadRepository, never()).save(any<Thread>())
    }

    @Test
    @DisplayName("스레드 목록 조회 - 일반 사용자")
    fun getThreads_Member_Success() {
        // given
        val page = PageImpl(listOf(testThread))

        whenever(threadRepository.findByUserId(eq(testUserId), any())).thenReturn(page)

        // when
        val result = chatService.getThreads(principal, 0, 10, "desc")

        // then
        assertNotNull(result)
        assertEquals(1, result.threads.size)
        verify(threadRepository).findByUserId(eq(testUserId), any())
    }

    @Test
    @DisplayName("스레드 목록 조회 - 관리자")
    fun getThreads_Admin_Success() {
        // given
        val adminPrincipal = UserPrincipal(
            id = testUserId,
            email = "admin@example.com",
            role = Role.ADMIN
        )
        val page = PageImpl(listOf(testThread))

        whenever(threadRepository.findAllBy(any())).thenReturn(page)

        // when
        val result = chatService.getThreads(adminPrincipal, 0, 10, "desc")

        // then
        assertNotNull(result)
        verify(threadRepository).findAllBy(any())
    }

    @Test
    @DisplayName("스레드 삭제 - 소유자")
    fun deleteThread_Owner_Success() {
        // given
        whenever(threadRepository.findById(testThreadId)).thenReturn(Optional.of(testThread))

        // when
        chatService.deleteThread(testThreadId, principal)

        // then
        verify(threadRepository).delete(testThread)
    }

    @Test
    @DisplayName("스레드 삭제 - 관리자")
    fun deleteThread_Admin_Success() {
        // given
        val adminPrincipal = UserPrincipal(
            id = UUID.randomUUID(),
            email = "admin@example.com",
            role = Role.ADMIN
        )
        whenever(threadRepository.findById(testThreadId)).thenReturn(Optional.of(testThread))

        // when
        chatService.deleteThread(testThreadId, adminPrincipal)

        // then
        verify(threadRepository).delete(testThread)
    }

    @Test
    @DisplayName("스레드 삭제 실패 - 권한 없음")
    fun deleteThread_AccessDenied() {
        // given
        val otherPrincipal = UserPrincipal(
            id = UUID.randomUUID(),
            email = "other@example.com",
            role = Role.MEMBER
        )
        whenever(threadRepository.findById(testThreadId)).thenReturn(Optional.of(testThread))

        // when & then
        assertThrows(AccessDeniedException::class.java) {
            chatService.deleteThread(testThreadId, otherPrincipal)
        }
    }

    @Test
    @DisplayName("스레드 삭제 실패 - 스레드 없음")
    fun deleteThread_ThreadNotFound() {
        // given
        whenever(threadRepository.findById(testThreadId)).thenReturn(Optional.empty())

        // when & then
        assertThrows(ThreadNotFoundException::class.java) {
            chatService.deleteThread(testThreadId, principal)
        }
    }
}
