package com.ingjuanocampo.enfila.backend.data.repositories

import com.ingjuanocampo.enfila.backend.data.database.NotificationsTable
import com.ingjuanocampo.enfila.backend.data.models.NotificationEventType
import com.ingjuanocampo.enfila.backend.data.models.NotificationInsert
import com.ingjuanocampo.enfila.backend.data.models.NotificationStatus
import com.ingjuanocampo.enfila.backend.data.models.QueuedNotification
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import java.sql.SQLException
import java.time.Instant

interface NotificationRepository {
    suspend fun insertIfAbsent(insert: NotificationInsert): Boolean
    suspend fun claimDue(now: Instant, limit: Int): List<QueuedNotification>
    suspend fun markSent(id: String, providerMessageId: String?)
    suspend fun markFailed(id: String, error: String, attempts: Int, nextAttemptAt: Instant)
}

class NotificationRepositoryImpl : NotificationRepository {

    override suspend fun insertIfAbsent(insert: NotificationInsert): Boolean = newSuspendedTransaction {
        val alreadyQueued = NotificationsTable.select {
            (NotificationsTable.shiftId eq insert.shiftId) and
                (NotificationsTable.eventType eq insert.eventType) and
                (NotificationsTable.attentionStartDate eq insert.attentionStartDate)
        }.count() > 0
        if (alreadyQueued) return@newSuspendedTransaction false

        try {
            val now = Instant.now()
            NotificationsTable.insert {
                it[id] = EntityID(insert.id, NotificationsTable)
                it[shiftId] = insert.shiftId
                it[contactId] = insert.contactId
                it[eventType] = insert.eventType
                it[body] = insert.body
                it[contentVariables] = insert.contentVariables
                it[status] = NotificationStatus.PENDING
                it[attempts] = 0
                it[nextAttemptAt] = now
                it[attentionStartDate] = insert.attentionStartDate
                it[createdAt] = now
                it[updatedAt] = now
            }
            true
        } catch (e: Exception) {
            if (isUniqueViolation(e)) false else throw e
        }
    }

    override suspend fun claimDue(now: Instant, limit: Int): List<QueuedNotification> = newSuspendedTransaction {
        requeueInterruptedSends(now)

        val due = NotificationsTable.select {
            NotificationsTable.status.inList(listOf(NotificationStatus.PENDING, NotificationStatus.FAILED)) and
                (NotificationsTable.attempts less 5) and
                (NotificationsTable.nextAttemptAt lessEq now)
        }
            .orderBy(NotificationsTable.nextAttemptAt to SortOrder.ASC)
            .limit(limit)
            .map { it.toQueuedNotification() }

        due.mapNotNull { row ->
            val claimed = try {
                NotificationsTable.update({
                    (NotificationsTable.id eq EntityID(row.id, NotificationsTable)) and
                        (NotificationsTable.status eq row.status)
                }) {
                    it[status] = NotificationStatus.SENDING
                    it[updatedAt] = now
                }
            } catch (_: Exception) {
                0
            }
            if (claimed == 1) row.copy(status = NotificationStatus.SENDING) else null
        }
    }

    override suspend fun markSent(id: String, providerMessageId: String?) = newSuspendedTransaction {
        NotificationsTable.update({
            (NotificationsTable.id eq EntityID(id, NotificationsTable)) and
                (NotificationsTable.status eq NotificationStatus.SENDING)
        }) {
            it[status] = NotificationStatus.SENT
            it[NotificationsTable.providerMessageId] = providerMessageId
            it[updatedAt] = Instant.now()
        }
        Unit
    }

    override suspend fun markFailed(
        id: String,
        error: String,
        attempts: Int,
        nextAttemptAt: Instant,
    ) = newSuspendedTransaction {
        NotificationsTable.update({
            (NotificationsTable.id eq EntityID(id, NotificationsTable)) and
                (NotificationsTable.status eq NotificationStatus.SENDING)
        }) {
            it[status] = NotificationStatus.FAILED
            it[NotificationsTable.attempts] = attempts
            it[lastError] = error
            it[NotificationsTable.nextAttemptAt] = nextAttemptAt
            it[updatedAt] = Instant.now()
        }
        Unit
    }

    private fun requeueInterruptedSends(now: Instant) {
        val staleBefore = now.minusSeconds(120)
        val stale = NotificationsTable.select {
            (NotificationsTable.status eq NotificationStatus.SENDING) and
                (NotificationsTable.updatedAt less staleBefore) and
                (NotificationsTable.attempts less 5)
        }.map { it.toQueuedNotification() }

        stale.forEach { row ->
            NotificationsTable.update({
                (NotificationsTable.id eq EntityID(row.id, NotificationsTable)) and
                    (NotificationsTable.status eq NotificationStatus.SENDING)
            }) {
                it[status] = NotificationStatus.FAILED
                it[attempts] = row.attempts + 1
                it[lastError] = "send interrupted before completion"
                it[nextAttemptAt] = now
                it[updatedAt] = now
            }
        }
    }

    private fun ResultRow.toQueuedNotification() = QueuedNotification(
        id = this[NotificationsTable.id].value,
        shiftId = this[NotificationsTable.shiftId],
        contactId = this[NotificationsTable.contactId],
        eventType = this[NotificationsTable.eventType],
        body = this[NotificationsTable.body],
        contentVariables = this[NotificationsTable.contentVariables],
        status = this[NotificationsTable.status],
        attempts = this[NotificationsTable.attempts],
        nextAttemptAt = this[NotificationsTable.nextAttemptAt],
        lastError = this[NotificationsTable.lastError],
        providerMessageId = this[NotificationsTable.providerMessageId],
        attentionStartDate = this[NotificationsTable.attentionStartDate],
    )
}

private fun isUniqueViolation(error: Throwable): Boolean {
    var current: Throwable? = error
    while (current != null) {
        if (current is SQLException && current.sqlState == "23505") return true
        current = current.cause
    }
    return false
}
