package bsm.insert.aiassignment.domain.auth.presentation

import bsm.insert.aiassignment.domain.auth.application.dto.request.AuthLoginRequest
import bsm.insert.aiassignment.domain.auth.application.dto.request.AuthSignUpRequest
import bsm.insert.aiassignment.domain.auth.application.dto.response.UserResponse
import bsm.insert.aiassignment.domain.auth.application.service.AuthService
import bsm.insert.aiassignment.global.properties.JwtProperties
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val jwtProperties: JwtProperties
) {

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    fun signUp(@Valid @RequestBody request: AuthSignUpRequest): ResponseEntity<UserResponse> {
        val response = authService.signUp(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    fun login(
        @Valid @RequestBody request: AuthLoginRequest,
        response: HttpServletResponse
    ) {
        val token = authService.login(request)
        response.setHeader(jwtProperties.header, "${jwtProperties.prefix} $token")
    }
}
