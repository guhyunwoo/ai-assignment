package bsm.insert.aiassignment.global.exception

import org.springframework.http.HttpStatus

open class BusinessException(
    val status: HttpStatus,
    override val message: String
) : RuntimeException(message)

class DuplicateEmailException : BusinessException(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다")

class UserNotFoundException : BusinessException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다")

class InvalidPasswordException : BusinessException(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다")

class InvalidTokenException : BusinessException(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다")

class AccessDeniedException : BusinessException(HttpStatus.FORBIDDEN, "접근 권한이 없습니다")

class ThreadNotFoundException : BusinessException(HttpStatus.NOT_FOUND, "스레드를 찾을 수 없습니다")

class ChatNotFoundException : BusinessException(HttpStatus.NOT_FOUND, "대화를 찾을 수 없습니다")

class FeedbackNotFoundException : BusinessException(HttpStatus.NOT_FOUND, "피드백을 찾을 수 없습니다")

class DuplicateFeedbackException : BusinessException(HttpStatus.CONFLICT, "이미 해당 대화에 피드백을 작성하셨습니다")
