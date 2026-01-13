package bsm.insert.aiassignment.domain.chat.application.exception

import bsm.insert.aiassignment.global.exception.ErrorMessage
import bsm.insert.aiassignment.global.exception.BusinessBaseException

class ThreadNotFoundException: BusinessBaseException(ErrorMessage.THREAD_NOT_FOUND)