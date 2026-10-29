package com.ingjuanocampo.enfila.backend.services

import com.ingjuanocampo.enfila.backend.data.models.Shift
import com.ingjuanocampo.enfila.backend.data.models.ShiftState
import com.ingjuanocampo.enfila.backend.data.models.UpdateShiftRequest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

data class TurnReadyDraft(
    val attentionStartDate: Long,
    val body: String,
    val contactDigits: String,
    val contentVariables: String,
)

data class PreparedShiftUpdate(
    val request: UpdateShiftRequest,
    val draft: TurnReadyDraft?,
)

fun normalizePhoneDigits(raw: String): String = raw.filter { it.isDigit() }

fun prepareShiftUpdate(
    previous: Shift,
    request: UpdateShiftRequest,
    siteName: String?,
    now: Long,
): PreparedShiftUpdate {
    val enteringCalling = request.state == ShiftState.CALLING && previous.state != ShiftState.CALLING
    if (!enteringCalling) return PreparedShiftUpdate(request, null)

    val attentionStartDate = request.attentionStartDate ?: now
    val preparedRequest = if (request.attentionStartDate == null) {
        request.copy(attentionStartDate = attentionStartDate)
    } else {
        request
    }
    val digits = normalizePhoneDigits(previous.contactId)
    if (digits.isEmpty()) return PreparedShiftUpdate(preparedRequest, null)

    val number = request.number ?: previous.number
    val place = siteName?.trim()?.takeIf { it.isNotEmpty() } ?: "el local"
    return PreparedShiftUpdate(
        request = preparedRequest,
        draft = TurnReadyDraft(
            attentionStartDate = attentionStartDate,
            body = "Es tu turno #$number en $place. Acércate por favor.",
            contactDigits = digits,
            contentVariables = buildJsonObject {
                put("1", number.toString())
                put("2", place)
            }.toString(),
        ),
    )
}
