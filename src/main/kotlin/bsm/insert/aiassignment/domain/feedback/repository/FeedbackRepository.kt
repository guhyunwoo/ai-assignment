package bsm.insert.aiassignment.domain.feedback.repository

import bsm.insert.aiassignment.domain.feedback.domain.Feedback
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FeedbackRepository : JpaRepository<Feedback, Long> {

    fun existsByUserIdAndChatId(userId: Long, chatId: Long): Boolean

    fun findByUserId(userId: Long, pageable: Pageable): Page<Feedback>

    fun findByUserIdAndIsPositive(userId: Long, isPositive: Boolean, pageable: Pageable): Page<Feedback>

    fun findByIsPositive(isPositive: Boolean, pageable: Pageable): Page<Feedback>

    fun findAllBy(pageable: Pageable): Page<Feedback>
}
