package bsm.insert.aiassignment.domain.feedback.service

import bsm.insert.aiassignment.domain.chat.entity.Chat
import bsm.insert.aiassignment.domain.chat.entity.Thread
import bsm.insert.aiassignment.domain.chat.repository.ChatRepository
import bsm.insert.aiassignment.domain.feedback.dto.FeedbackCreateRequest
import bsm.insert.aiassignment.domain.feedback.dto.FeedbackStatusUpdateRequest
import bsm.insert.aiassignment.domain.feedback.entity.Feedback
import bsm.insert.aiassignment.domain.feedback.entity.FeedbackStatus
import bsm.insert.aiassignment.domain.feedback.repository.FeedbackRepository
import bsm.insert.aiassignment.domain.user.entity.Role
import bsm.insert.aiassignment.domain.user.entity.User
import bsm.insert.aiassignment.domain.user.repository.UserRepository
import bsm.insert.aiassignment.global.exception.AccessDeniedException
import bsm.insert.aiassignment.global.exception.DuplicateFeedbackException
import bsm.insert.aiassignment.global.exception.FeedbackNotFoundException
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
import java.util.*

@ExtendWith(MockitoExtension::class)
class FeedbackServiceTest {

    @Mock
    private lateinit var feedbackRepository: FeedbackRepository

    @Mock
    private lateinit var chatRepository: ChatRepository

    @Mock
    private lateinit var userRepository: UserRepository

    @InjectMocks
    private lateinit var feedbackService: FeedbackService

    private lateinit var testUser: User
    private lateinit var testThread: Thread
    private lateinit var testChat: Chat
    private lateinit var testFeedback: Feedback
    private lateinit var principal: UserPrincipal
    private val testUserId = UUID.randomUUID()
    private val testChatId = UUID.randomUUID()
    private val testFeedbackId = UUID.randomUUID()

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
            id = UUID.randomUUID(),
            user = testUser
        )

        testChat = Chat(
            id = testChatId,
            question = "Hello",
            answer = "Hi there!",
            thread = testThread
        )

        testFeedback = Feedback(
            id = testFeedbackId,
            user = testUser,
            chat = testChat,
            isPositive = true,
            status = FeedbackStatus.PENDING
        )

        principal = UserPrincipal(
            id = testUserId,
            email = "test@example.com",
            role = Role.MEMBER
        )
    }

    @Test
    @DisplayName("피드백 생성 성공")
    fun createFeedback_Success() {
        // given
        val request = FeedbackCreateRequest(chatId = testChatId, isPositive = true)

        whenever(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser))
        whenever(chatRepository.findById(testChatId)).thenReturn(Optional.of(testChat))
        whenever(feedbackRepository.existsByUserIdAndChatId(testUserId, testChatId)).thenReturn(false)
        whenever(feedbackRepository.save(any<Feedback>())).thenReturn(testFeedback)

        // when
        val result = feedbackService.createFeedback(request, principal)

        // then
        assertNotNull(result)
        assertTrue(result.isPositive)
        assertEquals(FeedbackStatus.PENDING, result.status)
    }

    @Test
    @DisplayName("피드백 생성 실패 - 중복 피드백")
    fun createFeedback_Duplicate_Fail() {
        // given
        val request = FeedbackCreateRequest(chatId = testChatId, isPositive = true)

        whenever(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser))
        whenever(chatRepository.findById(testChatId)).thenReturn(Optional.of(testChat))
        whenever(feedbackRepository.existsByUserIdAndChatId(testUserId, testChatId)).thenReturn(true)

        // when & then
        assertThrows(DuplicateFeedbackException::class.java) {
            feedbackService.createFeedback(request, principal)
        }
    }

    @Test
    @DisplayName("피드백 생성 실패 - 다른 사용자의 대화")
    fun createFeedback_OtherUserChat_Fail() {
        // given
        val otherUser = User(
            id = UUID.randomUUID(),
            email = "other@example.com",
            password = "password",
            name = "Other User",
            role = Role.MEMBER
        )
        val otherThread = Thread(
            id = UUID.randomUUID(),
            user = otherUser
        )
        val otherChat = Chat(
            id = UUID.randomUUID(),
            question = "Hello",
            answer = "Hi",
            thread = otherThread
        )

        val request = FeedbackCreateRequest(chatId = otherChat.id!!, isPositive = true)

        whenever(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser))
        whenever(chatRepository.findById(otherChat.id!!)).thenReturn(Optional.of(otherChat))

        // when & then
        assertThrows(AccessDeniedException::class.java) {
            feedbackService.createFeedback(request, principal)
        }
    }

    @Test
    @DisplayName("피드백 목록 조회 - 일반 사용자")
    fun getFeedbacks_Member_Success() {
        // given
        val page = PageImpl(listOf(testFeedback))
        whenever(feedbackRepository.findByUserId(eq(testUserId), any())).thenReturn(page)

        // when
        val result = feedbackService.getFeedbacks(principal, 0, 10, "desc", null)

        // then
        assertNotNull(result)
        assertEquals(1, result.feedbacks.size)
    }

    @Test
    @DisplayName("피드백 목록 조회 - 관리자")
    fun getFeedbacks_Admin_Success() {
        // given
        val adminPrincipal = UserPrincipal(
            id = testUserId,
            email = "admin@example.com",
            role = Role.ADMIN
        )
        val page = PageImpl(listOf(testFeedback))
        whenever(feedbackRepository.findAllBy(any())).thenReturn(page)

        // when
        val result = feedbackService.getFeedbacks(adminPrincipal, 0, 10, "desc", null)

        // then
        assertNotNull(result)
        verify(feedbackRepository).findAllBy(any())
    }

    @Test
    @DisplayName("피드백 상태 변경 - 관리자")
    fun updateFeedbackStatus_Admin_Success() {
        // given
        val adminPrincipal = UserPrincipal(
            id = UUID.randomUUID(),
            email = "admin@example.com",
            role = Role.ADMIN
        )
        val request = FeedbackStatusUpdateRequest(status = FeedbackStatus.RESOLVED)

        whenever(feedbackRepository.findById(testFeedbackId)).thenReturn(Optional.of(testFeedback))
        whenever(feedbackRepository.save(any<Feedback>())).thenReturn(testFeedback)

        // when
        val result = feedbackService.updateFeedbackStatus(testFeedbackId, request, adminPrincipal)

        // then
        assertNotNull(result)
        verify(feedbackRepository).save(any<Feedback>())
    }

    @Test
    @DisplayName("피드백 상태 변경 실패 - 권한 없음")
    fun updateFeedbackStatus_NotAdmin_Fail() {
        // given
        val request = FeedbackStatusUpdateRequest(status = FeedbackStatus.RESOLVED)

        // when & then
        assertThrows(AccessDeniedException::class.java) {
            feedbackService.updateFeedbackStatus(testFeedbackId, request, principal)
        }
    }

    @Test
    @DisplayName("피드백 상태 변경 실패 - 피드백 없음")
    fun updateFeedbackStatus_NotFound_Fail() {
        // given
        val adminPrincipal = UserPrincipal(
            id = UUID.randomUUID(),
            email = "admin@example.com",
            role = Role.ADMIN
        )
        val request = FeedbackStatusUpdateRequest(status = FeedbackStatus.RESOLVED)

        whenever(feedbackRepository.findById(testFeedbackId)).thenReturn(Optional.empty())

        // when & then
        assertThrows(FeedbackNotFoundException::class.java) {
            feedbackService.updateFeedbackStatus(testFeedbackId, request, adminPrincipal)
        }
    }
}
