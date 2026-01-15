package com.ingjuanocampo.enfila.backend.data.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val phone: String,
    val name: String? = "",
    val companyIds: List<String>? = null
)

@Serializable
data class CreateUserRequest(
    val id: String,
    val phone: String,
    val name: String? = "",
    val companyIds: List<String>? = null
)

@Serializable
data class UpdateUserRequest(
    val name: String? = null,
    val companyIds: List<String>? = null
)
