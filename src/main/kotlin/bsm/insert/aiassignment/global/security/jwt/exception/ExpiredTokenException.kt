package bsm.insert.aiassignment.global.security.jwt.exception

import bsm.insert.aiassignment.global.exception.ErrorMessage
import bsm.insert.aiassignment.global.exception.BusinessBaseException

class ExpiredTokenException : BusinessBaseException(ErrorMessage.EXPIRED_TOKEN)
