package bsm.insert.aiassignment.domain.feedback.application.exception

import bsm.insert.aiassignment.global.exception.ErrorMessage
import bsm.insert.aiassignment.global.exception.BusinessBaseException

class ChatNotFoundException: BusinessBaseException(ErrorMessage.CHAT_NOT_FOUND)