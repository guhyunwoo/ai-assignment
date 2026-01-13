package bsm.insert.aiassignment.domain.user.application.exception

import bsm.insert.aiassignment.global.exception.ErrorMessage
import bsm.insert.aiassignment.global.exception.BusinessBaseException

class UserNotFoundException: BusinessBaseException(ErrorMessage.USER_NOT_FOUND)