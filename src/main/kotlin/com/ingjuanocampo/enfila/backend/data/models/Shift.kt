package com.ingjuanocampo.enfila.backend.data.models

import kotlinx.serialization.Serializable

@Serializable
enum class ShiftState {
    WAITING,
    CALLING,
    CANCELLED,
    FINISHED
}

@Serializable
data class Shift(
    val id: String,
    val date: Long, // Create date
    val parentCompanySite: String,
    val number: Int = 0,
    val contactId: String,
    val notes: String?,
    val state: ShiftState,
    val attentionStartDate: Long? = null,
    val endDate: Long? = null
)

@Serializable
data class CreateShiftRequest(
    val parentCompanySite: String,
    val number: Int,
    val contactId: String,
    val notes: String?
)

@Serializable
data class UpdateShiftRequest(
    val number: Int? = null,
    val notes: String? = null,
    val state: ShiftState? = null,
    val attentionStartDate: Long? = null,
    val endDate: Long? = null
)

@Serializable
data class ShiftDetails(
    val shift: Shift,
    val client: Client?
)
