package com.ingjuanocampo.enfila.backend.data.models

import kotlinx.serialization.Serializable

@Serializable
enum class TipMilestone {
    ON_LOGIN,
    FIRST_SHIFT_ASSIGNED,
    FIRST_SHIFT_CALLED,
    FIRST_SHIFT_COMPLETED,
    HAS_CLIENTS,
    PROFILE_COMPLETE,
}

@Serializable
data class Tip(
    val id: String,
    val order: Int,
    val question: String,
    val answer: String,
    val milestone: TipMilestone,
    val published: Boolean = true,
)

@Serializable
data class TipWithStatus(
    val id: String,
    val order: Int,
    val question: String,
    val answer: String,
    val milestone: TipMilestone,
    val isUnlocked: Boolean,
)

@Serializable
data class PublishTipRequest(
    val id: String? = null,
    val order: Int,
    val question: String,
    val answer: String,
    val milestone: TipMilestone,
    val published: Boolean = true,
)
