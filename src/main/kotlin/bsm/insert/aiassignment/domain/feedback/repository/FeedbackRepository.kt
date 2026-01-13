package bsm.insert.aiassignment.domain.feedback.repository

import bsm.insert.aiassignment.domain.feedback.entity.Feedback
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface FeedbackRepository : JpaRepository<Feedback, UUID> {

    fun existsByUserIdAndChatId(userId: UUID, chatId: UUID): Boolean

    fun findByUserId(userId: UUID, pageable: Pageable): Page<Feedback>

    fun findByUserIdAndIsPositive(userId: UUID, isPositive: Boolean, pageable: Pageable): Page<Feedback>

    fun findByIsPositive(isPositive: Boolean, pageable: Pageable): Page<Feedback>

    fun findAllBy(pageable: Pageable): Page<Feedback>
}
