package bsm.insert.aiassignment.domain.user.service

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
    private val jwtProvider: JwtProvider
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
        return UserResponse.from(savedUser)
    }

    fun login(request: LoginRequest): LoginResponse {
        val user = userRepository.findByEmail(request.email)
            ?: throw UserNotFoundException()

        if (!passwordEncoder.matches(request.password, user.password)) {
            throw InvalidPasswordException()
        }

        val token = jwtProvider.generateToken(user.id!!, user.email, user.role)
        return LoginResponse(token = token, user = UserResponse.from(user))
    }
}
