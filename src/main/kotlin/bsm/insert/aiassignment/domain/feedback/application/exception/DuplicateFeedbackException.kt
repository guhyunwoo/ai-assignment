package bsm.insert.aiassignment.domain.feedback.application.exception

import bsm.insert.aiassignment.global.exception.ErrorMessage
import bsm.insert.aiassignment.global.exception.BusinessBaseException

class DuplicateFeedbackException: BusinessBaseException(ErrorMessage.DUPLICATE_FEEDBACK)