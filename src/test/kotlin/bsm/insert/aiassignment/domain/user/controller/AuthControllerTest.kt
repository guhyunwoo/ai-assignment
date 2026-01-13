package bsm.insert.aiassignment.domain.user.controller

import bsm.insert.aiassignment.domain.user.dto.LoginRequest
import bsm.insert.aiassignment.domain.user.dto.LoginResponse
import bsm.insert.aiassignment.domain.user.dto.SignUpRequest
import bsm.insert.aiassignment.domain.user.dto.UserResponse
import bsm.insert.aiassignment.domain.user.entity.Role
import bsm.insert.aiassignment.domain.user.service.UserService
import bsm.insert.aiassignment.global.exception.DuplicateEmailException
import bsm.insert.aiassignment.global.exception.GlobalExceptionHandler
import bsm.insert.aiassignment.global.exception.InvalidPasswordException
import bsm.insert.aiassignment.global.exception.UserNotFoundException
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.OffsetDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class AuthControllerTest {

    private lateinit var mockMvc: MockMvc

    @Mock
    private lateinit var userService: UserService

    @InjectMocks
    private lateinit var authController: AuthController

    private val objectMapper = ObjectMapper()
    private val testUserId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
            .setControllerAdvice(GlobalExceptionHandler())
            .build()
    }

    @Test
    @DisplayName("회원가입 API - 성공")
    fun signUp_Success() {
        // given
        val request = SignUpRequest(
            email = "test@example.com",
            password = "password123",
            name = "Test User"
        )
        val response = UserResponse(
            id = testUserId,
            email = "test@example.com",
            name = "Test User",
            role = Role.MEMBER,
            createdAt = OffsetDateTime.now()
        )
        whenever(userService.signUp(any())).thenReturn(response)

        // when & then
        mockMvc.post("/api/auth/signup") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isCreated() }
            jsonPath("$.email") { value("test@example.com") }
            jsonPath("$.name") { value("Test User") }
        }
    }

    @Test
    @DisplayName("회원가입 API - 중복 이메일 실패")
    fun signUp_DuplicateEmail_Fail() {
        // given
        val request = SignUpRequest(
            email = "test@example.com",
            password = "password123",
            name = "Test User"
        )
        whenever(userService.signUp(any())).thenThrow(DuplicateEmailException())

        // when & then
        mockMvc.post("/api/auth/signup") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isConflict() }
        }
    }

    @Test
    @DisplayName("회원가입 API - 유효성 검사 실패")
    fun signUp_ValidationFail() {
        // given
        val request = mapOf(
            "email" to "invalid-email",
            "password" to "short",
            "name" to ""
        )

        // when & then
        mockMvc.post("/api/auth/signup") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    @DisplayName("로그인 API - 성공")
    fun login_Success() {
        // given
        val request = LoginRequest(
            email = "test@example.com",
            password = "password123"
        )
        val userResponse = UserResponse(
            id = testUserId,
            email = "test@example.com",
            name = "Test User",
            role = Role.MEMBER,
            createdAt = OffsetDateTime.now()
        )
        val response = LoginResponse(token = "jwt-token", user = userResponse)
        whenever(userService.login(any())).thenReturn(response)

        // when & then
        mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isOk() }
            jsonPath("$.token") { value("jwt-token") }
        }
    }

    @Test
    @DisplayName("로그인 API - 사용자 없음 실패")
    fun login_UserNotFound_Fail() {
        // given
        val request = LoginRequest(
            email = "notfound@example.com",
            password = "password123"
        )
        whenever(userService.login(any())).thenThrow(UserNotFoundException())

        // when & then
        mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isNotFound() }
        }
    }

    @Test
    @DisplayName("로그인 API - 비밀번호 불일치 실패")
    fun login_InvalidPassword_Fail() {
        // given
        val request = LoginRequest(
            email = "test@example.com",
            password = "wrongpassword"
        )
        whenever(userService.login(any())).thenThrow(InvalidPasswordException())

        // when & then
        mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isUnauthorized() }
        }
    }
}
