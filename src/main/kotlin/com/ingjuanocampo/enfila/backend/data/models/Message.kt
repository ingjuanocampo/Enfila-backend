package com.ingjuanocampo.enfila.backend.data.models

import kotlinx.serialization.Serializable

@Serializable
data class SendMessageRequest(
    val to: String,
    val from: String,
    val body: String
)

@Serializable
data class MessageResponse(
    val success: Boolean,
    val messageId: String? = null,
    val error: String? = null
)

@Serializable
data class TwilioCredentials(
    val sid: String,
    val token: String
)
