package bsm.insert.aiassignment.global.security.jwt.exception

import bsm.insert.aiassignment.global.exception.ErrorMessage
import bsm.insert.aiassignment.global.exception.BusinessBaseException

class InvalidTokenException : BusinessBaseException(ErrorMessage.INVALID_TOKEN)
