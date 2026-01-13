package bsm.insert.aiassignment.domain.auth.application.service

import bsm.insert.aiassignment.domain.analytics.domain.type.ActivityType
import bsm.insert.aiassignment.domain.analytics.application.service.AnalyticsService
import bsm.insert.aiassignment.domain.auth.application.dto.request.AuthLoginRequest
import bsm.insert.aiassignment.domain.auth.application.dto.request.AuthSignUpRequest
import bsm.insert.aiassignment.domain.auth.application.dto.response.UserResponse
import bsm.insert.aiassignment.domain.auth.application.exception.DuplicateEmailException
import bsm.insert.aiassignment.domain.auth.application.exception.InvalidPasswordException
import bsm.insert.aiassignment.domain.common.snowflake.Snowflake
import bsm.insert.aiassignment.domain.user.application.exception.UserNotFoundException
import bsm.insert.aiassignment.domain.user.domain.User
import bsm.insert.aiassignment.domain.user.repository.UserRepository
import bsm.insert.aiassignment.global.security.jwt.TokenProvider
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val tokenProvider: TokenProvider,
    private val analyticsService: AnalyticsService,
    private val snowflake: Snowflake,
) {

    @Transactional
    fun signUp(request: AuthSignUpRequest): UserResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw DuplicateEmailException()
        }

        val user = User(
            id = snowflake.nextId(),
            email = request.email,
            password = passwordEncoder.encode(request.password),
            name = request.name
        )

        val savedUser = userRepository.save(user)
        analyticsService.logActivity(savedUser, ActivityType.SIGNUP)
        return UserResponse.from(savedUser)
    }

    @Transactional
    fun login(request: AuthLoginRequest): String {
        val user = userRepository.findByEmail(request.email)
            ?: throw UserNotFoundException()

        if (!passwordEncoder.matches(request.password, user.password)) {
            throw InvalidPasswordException()
        }

        analyticsService.logActivity(user, ActivityType.LOGIN)
        return tokenProvider.createAccessToken(user.id, user.role)
    }
}
