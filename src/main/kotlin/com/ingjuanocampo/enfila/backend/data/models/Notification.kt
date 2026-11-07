package com.ingjuanocampo.enfila.backend.data.models

import java.time.Instant

enum class NotificationEventType {
    TURN_READY,
}

enum class NotificationStatus {
    PENDING,
    SENDING,
    SENT,
    FAILED,
}

data class NotificationInsert(
    val id: String,
    val shiftId: String,
    val contactId: String,
    val eventType: NotificationEventType,
    val body: String,
    val contentVariables: String?,
    val attentionStartDate: Long,
)

data class QueuedNotification(
    val id: String,
    val shiftId: String,
    val contactId: String,
    val eventType: NotificationEventType,
    val body: String,
    val contentVariables: String?,
    val status: NotificationStatus,
    val attempts: Int,
    val nextAttemptAt: Instant,
    val lastError: String?,
    val providerMessageId: String?,
    val attentionStartDate: Long,
)

data class MessageSendResult(
    val success: Boolean,
    val messageId: String? = null,
    val error: String? = null,
)
