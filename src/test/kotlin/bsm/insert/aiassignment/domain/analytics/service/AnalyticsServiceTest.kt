package bsm.insert.aiassignment.domain.analytics.service

import bsm.insert.aiassignment.domain.analytics.entity.ActivityLog
import bsm.insert.aiassignment.domain.analytics.entity.ActivityType
import bsm.insert.aiassignment.domain.analytics.repository.ActivityLogRepository
import bsm.insert.aiassignment.domain.chat.entity.Chat
import bsm.insert.aiassignment.domain.chat.entity.Thread
import bsm.insert.aiassignment.domain.chat.repository.ChatRepository
import bsm.insert.aiassignment.domain.user.entity.Role
import bsm.insert.aiassignment.domain.user.entity.User
import bsm.insert.aiassignment.global.exception.AccessDeniedException
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
import java.time.OffsetDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class AnalyticsServiceTest {

    @Mock
    private lateinit var activityLogRepository: ActivityLogRepository

    @Mock
    private lateinit var chatRepository: ChatRepository

    @InjectMocks
    private lateinit var analyticsService: AnalyticsService

    private lateinit var testUser: User
    private lateinit var adminPrincipal: UserPrincipal
    private lateinit var memberPrincipal: UserPrincipal
    private val testUserId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        testUser = User(
            id = testUserId,
            email = "test@example.com",
            password = "password",
            name = "Test User",
            role = Role.MEMBER
        )

        adminPrincipal = UserPrincipal(
            id = UUID.randomUUID(),
            email = "admin@example.com",
            role = Role.ADMIN
        )

        memberPrincipal = UserPrincipal(
            id = testUserId,
            email = "test@example.com",
            role = Role.MEMBER
        )
    }

    @Test
    @DisplayName("활동 통계 조회 성공 - 관리자")
    fun getActivityStats_Admin_Success() {
        // given
        whenever(activityLogRepository.countByActivityTypeAndCreatedAtBetween(
            eq(ActivityType.SIGNUP), any(), any()
        )).thenReturn(5L)

        whenever(activityLogRepository.countByActivityTypeAndCreatedAtBetween(
            eq(ActivityType.LOGIN), any(), any()
        )).thenReturn(10L)

        whenever(chatRepository.countByCreatedAtBetween(any(), any())).thenReturn(20L)

        // when
        val result = analyticsService.getActivityStats(adminPrincipal)

        // then
        assertNotNull(result)
        assertEquals(5L, result.signupCount)
        assertEquals(10L, result.loginCount)
        assertEquals(20L, result.chatCount)
    }

    @Test
    @DisplayName("활동 통계 조회 실패 - 권한 없음")
    fun getActivityStats_NotAdmin_Fail() {
        // when & then
        assertThrows(AccessDeniedException::class.java) {
            analyticsService.getActivityStats(memberPrincipal)
        }
    }

    @Test
    @DisplayName("보고서 생성 성공 - 관리자")
    fun generateChatReport_Admin_Success() {
        // given
        val thread = Thread(
            id = UUID.randomUUID(),
            user = testUser
        )

        val chat = Chat(
            id = UUID.randomUUID(),
            question = "Hello",
            answer = "Hi there!",
            thread = thread,
            createdAt = OffsetDateTime.now()
        )

        whenever(chatRepository.findByCreatedAtBetween(any(), any())).thenReturn(listOf(chat))

        // when
        val result = analyticsService.generateChatReport(adminPrincipal)

        // then
        assertNotNull(result)
        assertTrue(result.contains("chat_id"))
        assertTrue(result.contains("question"))
        assertTrue(result.contains("answer"))
        assertTrue(result.contains("Hello"))
        assertTrue(result.contains("Hi there!"))
    }

    @Test
    @DisplayName("보고서 생성 실패 - 권한 없음")
    fun generateChatReport_NotAdmin_Fail() {
        // when & then
        assertThrows(AccessDeniedException::class.java) {
            analyticsService.generateChatReport(memberPrincipal)
        }
    }

    @Test
    @DisplayName("활동 로그 기록")
    fun logActivity_Success() {
        // given
        whenever(activityLogRepository.save(any<ActivityLog>())).thenAnswer { it.arguments[0] }

        // when
        analyticsService.logActivity(testUser, ActivityType.SIGNUP)

        // then
        verify(activityLogRepository).save(any<ActivityLog>())
    }
}
