package com.ingjuanocampo.enfila.backend.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Client(
    val id: String, // Same as phone
    val name: String? = "",
    val shifts: List<String>? = emptyList()
)

@Serializable
data class CreateClientRequest(
    val id: String, // Phone number
    val name: String? = ""
)

@Serializable
data class UpdateClientRequest(
    val name: String? = null
)

@Serializable
data class ClientWithShifts(
    val id: String,
    val name: String?,
    val shifts: List<Shift>
)
