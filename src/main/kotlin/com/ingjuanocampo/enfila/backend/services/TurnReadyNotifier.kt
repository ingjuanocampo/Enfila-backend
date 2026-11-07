package com.ingjuanocampo.enfila.backend.services

import com.ingjuanocampo.enfila.backend.data.models.NotificationEventType
import com.ingjuanocampo.enfila.backend.data.models.NotificationInsert
import com.ingjuanocampo.enfila.backend.data.repositories.NotificationRepository
import java.util.UUID

class TurnReadyNotifier(
    private val notificationRepository: NotificationRepository,
) {
    suspend fun enqueue(shiftId: String, draft: TurnReadyDraft) {
        notificationRepository.insertIfAbsent(
            NotificationInsert(
                id = UUID.randomUUID().toString(),
                shiftId = shiftId,
                contactId = draft.contactDigits,
                eventType = NotificationEventType.TURN_READY,
                body = draft.body,
                contentVariables = draft.contentVariables,
                attentionStartDate = draft.attentionStartDate,
            ),
        )
    }
}
