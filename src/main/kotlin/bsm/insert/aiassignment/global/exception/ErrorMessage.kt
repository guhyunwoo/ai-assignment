package bsm.insert.aiassignment.global.exception

import org.springframework.http.HttpStatus

enum class ErrorMessage(
    val httpStatus: HttpStatus,
    val content: String
) {
    // JWT
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "잘못된 토큰 형식입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),

    // USER
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다."),

    // AUTH
    CANT_ACCESS(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    // THREAD
    THREAD_NOT_FOUND(HttpStatus.NOT_FOUND, "스레드를 찾을 수 없습니다."),

    // CHAT
    CHAT_NOT_FOUND(HttpStatus.NOT_FOUND, "대화를 찾을 수 없습니다."),

    // FEEDBACK
    FEEDBACK_NOT_FOUND(HttpStatus.NOT_FOUND, "피드백을 찾을 수 없습니다."),
    DUPLICATE_FEEDBACK(HttpStatus.CONFLICT, "이미 해당 대화에 피드백을 작성하셨습니다.")
}