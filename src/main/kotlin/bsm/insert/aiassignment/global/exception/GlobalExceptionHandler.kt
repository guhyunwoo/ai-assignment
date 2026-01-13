package bsm.insert.aiassignment.global.exception

import bsm.insert.aiassignment.global.exception.BusinessBaseException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

data class ErrorResponse(
    val status: Int,
    val message: String,
    val errors: List<String>? = null
)

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(BusinessBaseException::class)
    fun handleBusinessBaseException(e: BusinessBaseException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(e.getStatus()).body(
            ErrorResponse(
                status = e.getStatus().value(),
                message = e.getErrorMessageContent()
            )
        )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errors = e.bindingResult.fieldErrors.map { "${it.field}: ${it.defaultMessage}" }
        return ResponseEntity.badRequest().body(
            ErrorResponse(
                status = HttpStatus.BAD_REQUEST.value(),
                message = "입력값이 올바르지 않습니다",
                errors = errors
            )
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
        return ResponseEntity.internalServerError().body(
            ErrorResponse(
                status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                message = "서버 내부 오류가 발생했습니다"
            )
        )
    }
}
