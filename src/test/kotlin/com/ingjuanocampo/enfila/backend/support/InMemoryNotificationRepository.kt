package com.ingjuanocampo.enfila.backend.support

import com.ingjuanocampo.enfila.backend.data.models.NotificationInsert
import com.ingjuanocampo.enfila.backend.data.models.NotificationStatus
import com.ingjuanocampo.enfila.backend.data.models.QueuedNotification
import com.ingjuanocampo.enfila.backend.data.repositories.NotificationRepository
import java.time.Instant

class InMemoryNotificationRepository(
    private val clock: () -> Instant = { Instant.now() },
) : NotificationRepository {
    val rows = mutableListOf<QueuedNotification>()

    override suspend fun insertIfAbsent(insert: NotificationInsert): Boolean {
        val duplicate = rows.any {
            it.shiftId == insert.shiftId &&
                it.eventType == insert.eventType &&
                it.attentionStartDate == insert.attentionStartDate
        }
        if (duplicate) return false
        val now = clock()
        rows += QueuedNotification(
            id = insert.id,
            shiftId = insert.shiftId,
            contactId = insert.contactId,
            eventType = insert.eventType,
            body = insert.body,
            contentVariables = insert.contentVariables,
            status = NotificationStatus.PENDING,
            attempts = 0,
            nextAttemptAt = now,
            lastError = null,
            providerMessageId = null,
            attentionStartDate = insert.attentionStartDate,
        )
        return true
    }

    override suspend fun claimDue(now: Instant, limit: Int): List<QueuedNotification> {
        val due = rows.filter {
            (it.status == NotificationStatus.PENDING || it.status == NotificationStatus.FAILED) &&
                it.attempts < 5 &&
                !it.nextAttemptAt.isAfter(now)
        }.sortedBy { it.nextAttemptAt }.take(limit)

        return due.mapNotNull { row ->
            val index = rows.indexOfFirst { it.id == row.id && it.status == row.status }
            if (index < 0) return@mapNotNull null
            val claimed = rows[index].copy(status = NotificationStatus.SENDING)
            rows[index] = claimed
            claimed
        }
    }

    override suspend fun markSent(id: String, providerMessageId: String?) {
        update(id) { it.copy(status = NotificationStatus.SENT, providerMessageId = providerMessageId) }
    }

    override suspend fun markFailed(id: String, error: String, attempts: Int, nextAttemptAt: Instant) {
        update(id) {
            it.copy(
                status = NotificationStatus.FAILED,
                attempts = attempts,
                lastError = error,
                nextAttemptAt = nextAttemptAt,
            )
        }
    }

    private fun update(id: String, transform: (QueuedNotification) -> QueuedNotification) {
        val index = rows.indexOfFirst { it.id == id && it.status == NotificationStatus.SENDING }
        if (index >= 0) rows[index] = transform(rows[index])
    }
}
