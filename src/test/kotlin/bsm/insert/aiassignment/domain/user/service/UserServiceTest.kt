package bsm.insert.aiassignment.domain.user.service

import bsm.insert.aiassignment.domain.user.dto.LoginRequest
import bsm.insert.aiassignment.domain.user.dto.SignUpRequest
import bsm.insert.aiassignment.domain.user.entity.Role
import bsm.insert.aiassignment.domain.user.entity.User
import bsm.insert.aiassignment.domain.user.repository.UserRepository
import bsm.insert.aiassignment.global.exception.DuplicateEmailException
import bsm.insert.aiassignment.global.exception.InvalidPasswordException
import bsm.insert.aiassignment.global.exception.UserNotFoundException
import bsm.insert.aiassignment.global.security.JwtProvider
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.*

@ExtendWith(MockitoExtension::class)
class UserServiceTest {

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var passwordEncoder: PasswordEncoder

    @Mock
    private lateinit var jwtProvider: JwtProvider

    @InjectMocks
    private lateinit var userService: UserService

    private lateinit var testUser: User
    private val testUserId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        testUser = User(
            id = testUserId,
            email = "test@example.com",
            password = "encodedPassword",
            name = "Test User",
            role = Role.MEMBER
        )
    }

    @Test
    @DisplayName("회원가입 성공")
    fun signUp_Success() {
        // given
        val request = SignUpRequest(
            email = "test@example.com",
            password = "password123",
            name = "Test User"
        )
        whenever(userRepository.existsByEmail(request.email)).thenReturn(false)
        whenever(passwordEncoder.encode(request.password)).thenReturn("encodedPassword")
        whenever(userRepository.save(any<User>())).thenReturn(testUser)

        // when
        val result = userService.signUp(request)

        // then
        assertNotNull(result)
        assertEquals("test@example.com", result.email)
        assertEquals("Test User", result.name)
        assertEquals(Role.MEMBER, result.role)
        verify(userRepository).save(any<User>())
    }

    @Test
    @DisplayName("회원가입 실패 - 중복 이메일")
    fun signUp_DuplicateEmail_ThrowsException() {
        // given
        val request = SignUpRequest(
            email = "test@example.com",
            password = "password123",
            name = "Test User"
        )
        whenever(userRepository.existsByEmail(request.email)).thenReturn(true)

        // when & then
        assertThrows(DuplicateEmailException::class.java) {
            userService.signUp(request)
        }
        verify(userRepository, never()).save(any<User>())
    }

    @Test
    @DisplayName("로그인 성공")
    fun login_Success() {
        // given
        val request = LoginRequest(
            email = "test@example.com",
            password = "password123"
        )
        whenever(userRepository.findByEmail(request.email)).thenReturn(testUser)
        whenever(passwordEncoder.matches(request.password, testUser.password)).thenReturn(true)
        whenever(jwtProvider.generateToken(testUserId, testUser.email, testUser.role)).thenReturn("jwt-token")

        // when
        val result = userService.login(request)

        // then
        assertNotNull(result)
        assertEquals("jwt-token", result.token)
        assertEquals("test@example.com", result.user.email)
    }

    @Test
    @DisplayName("로그인 실패 - 사용자 없음")
    fun login_UserNotFound_ThrowsException() {
        // given
        val request = LoginRequest(
            email = "notfound@example.com",
            password = "password123"
        )
        whenever(userRepository.findByEmail(request.email)).thenReturn(null)

        // when & then
        assertThrows(UserNotFoundException::class.java) {
            userService.login(request)
        }
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    fun login_InvalidPassword_ThrowsException() {
        // given
        val request = LoginRequest(
            email = "test@example.com",
            password = "wrongpassword"
        )
        whenever(userRepository.findByEmail(request.email)).thenReturn(testUser)
        whenever(passwordEncoder.matches(request.password, testUser.password)).thenReturn(false)

        // when & then
        assertThrows(InvalidPasswordException::class.java) {
            userService.login(request)
        }
    }
}
