package bsm.insert.aiassignment.domain.chat.repository

import bsm.insert.aiassignment.domain.chat.entity.Thread
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ThreadRepository : JpaRepository<Thread, UUID> {

    @Query("SELECT t FROM Thread t WHERE t.user.id = :userId ORDER BY t.createdAt DESC")
    fun findLatestByUserId(userId: UUID, pageable: Pageable): List<Thread>

    fun findByUserId(userId: UUID, pageable: Pageable): Page<Thread>

    fun findAllBy(pageable: Pageable): Page<Thread>

    @Query("SELECT t FROM Thread t JOIN FETCH t.chats WHERE t.user.id = :userId")
    fun findByUserIdWithChats(userId: UUID, pageable: Pageable): Page<Thread>

    @Query("SELECT t FROM Thread t JOIN FETCH t.chats")
    fun findAllWithChats(pageable: Pageable): Page<Thread>
}
