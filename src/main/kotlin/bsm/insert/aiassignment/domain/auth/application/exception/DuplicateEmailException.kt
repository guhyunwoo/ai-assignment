package bsm.insert.aiassignment.domain.auth.application.exception

import bsm.insert.aiassignment.global.exception.ErrorMessage
import bsm.insert.aiassignment.global.exception.BusinessBaseException

class DuplicateEmailException: BusinessBaseException(ErrorMessage.DUPLICATE_EMAIL)
