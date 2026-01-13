package bsm.insert.aiassignment.domain.user.service

import bsm.insert.aiassignment.domain.analytics.entity.ActivityType
import bsm.insert.aiassignment.domain.analytics.service.AnalyticsService
import bsm.insert.aiassignment.domain.user.dto.*
import bsm.insert.aiassignment.domain.user.entity.User
import bsm.insert.aiassignment.domain.user.repository.UserRepository
import bsm.insert.aiassignment.global.exception.DuplicateEmailException
import bsm.insert.aiassignment.global.exception.InvalidPasswordException
import bsm.insert.aiassignment.global.exception.UserNotFoundException
import bsm.insert.aiassignment.global.security.JwtProvider
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtProvider: JwtProvider,
    private val analyticsService: AnalyticsService
) {

    @Transactional
    fun signUp(request: SignUpRequest): UserResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw DuplicateEmailException()
        }

        val user = User(
            email = request.email,
            password = passwordEncoder.encode(request.password),
            name = request.name
        )

        val savedUser = userRepository.save(user)
        analyticsService.logActivity(savedUser, ActivityType.SIGNUP)
        return UserResponse.from(savedUser)
    }

    @Transactional
    fun login(request: LoginRequest): LoginResponse {
        val user = userRepository.findByEmail(request.email)
            ?: throw UserNotFoundException()

        if (!passwordEncoder.matches(request.password, user.password)) {
            throw InvalidPasswordException()
        }

        analyticsService.logActivity(user, ActivityType.LOGIN)
        val token = jwtProvider.generateToken(user.id!!, user.email, user.role)
        return LoginResponse(token = token, user = UserResponse.from(user))
    }
}
