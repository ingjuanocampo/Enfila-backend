package com.ingjuanocampo.enfila.backend.services

import com.ingjuanocampo.enfila.backend.data.models.MessageSendResult
import com.ingjuanocampo.enfila.backend.data.repositories.NotificationRepository
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.Instant

interface TurnReadyTransport {
    suspend fun sendTurnReady(toDigits: String, body: String, contentVariablesJson: String?): MessageSendResult
}

class NotificationDeliveryWorker(
    private val notificationRepository: NotificationRepository,
    private val transport: TurnReadyTransport,
    private val clock: () -> Instant = { Instant.now() },
) {
    private val logger = LoggerFactory.getLogger(NotificationDeliveryWorker::class.java)

    suspend fun processOnce() {
        val claimed = notificationRepository.claimDue(clock(), limit = 20)
        for (notification in claimed) {
            val result = try {
                transport.sendTurnReady(
                    toDigits = notification.contactId,
                    body = notification.body,
                    contentVariablesJson = notification.contentVariables,
                )
            } catch (e: Exception) {
                MessageSendResult(success = false, error = e.message ?: "Failed to send message")
            }

            if (result.success) {
                notificationRepository.markSent(notification.id, result.messageId)
            } else {
                val attempts = notification.attempts + 1
                val error = result.error ?: "Failed to send message"
                logger.warn(
                    "Turn-ready notification {} failed (attempt {}): {}",
                    notification.id,
                    attempts,
                    error,
                )
                notificationRepository.markFailed(
                    id = notification.id,
                    error = error,
                    attempts = attempts,
                    nextAttemptAt = clock().plus(retryDelay(attempts)),
                )
            }
        }
    }
}

fun retryDelay(attempts: Int): Duration = when (attempts) {
    1 -> Duration.ofSeconds(15)
    2 -> Duration.ofMinutes(1)
    3 -> Duration.ofMinutes(5)
    else -> Duration.ofMinutes(15)
}
