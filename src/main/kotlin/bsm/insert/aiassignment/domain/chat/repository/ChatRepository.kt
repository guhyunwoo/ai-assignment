package bsm.insert.aiassignment.domain.chat.repository

import bsm.insert.aiassignment.domain.chat.entity.Chat
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime
import java.util.*

@Repository
interface ChatRepository : JpaRepository<Chat, UUID> {

    @Query("SELECT c FROM Chat c WHERE c.thread.id = :threadId ORDER BY c.createdAt ASC")
    fun findByThreadIdOrderByCreatedAtAsc(threadId: UUID): List<Chat>

    @Query("SELECT COUNT(c) FROM Chat c WHERE c.createdAt >= :startTime AND c.createdAt < :endTime")
    fun countByCreatedAtBetween(startTime: OffsetDateTime, endTime: OffsetDateTime): Long

    @Query("SELECT c FROM Chat c WHERE c.createdAt >= :startTime AND c.createdAt < :endTime")
    fun findByCreatedAtBetween(startTime: OffsetDateTime, endTime: OffsetDateTime): List<Chat>
}
