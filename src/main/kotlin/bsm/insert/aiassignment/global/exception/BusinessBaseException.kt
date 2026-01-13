package bsm.insert.aiassignment.global.exception

import bsm.insert.aiassignment.global.exception.ErrorMessage
import org.springframework.http.HttpStatus

abstract class BusinessBaseException(
    val errorMessage: ErrorMessage
) : RuntimeException(errorMessage.content) {
    fun getErrorMessageContent(): String = errorMessage.content
    fun getStatus(): HttpStatus = errorMessage.httpStatus
}