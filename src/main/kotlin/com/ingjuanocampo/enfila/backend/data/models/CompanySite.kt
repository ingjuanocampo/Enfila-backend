package com.ingjuanocampo.enfila.backend.data.models

import kotlinx.serialization.Serializable

@Serializable
data class CompanySite(
    val id: String,
    val name: String? = "",
    val shiftsIdList: List<String>? = null
)

@Serializable
data class CreateCompanySiteRequest(
    val name: String
)

@Serializable
data class UpdateCompanySiteRequest(
    val name: String? = null
)

@Serializable
data class CompanySiteWithShifts(
    val id: String,
    val name: String?,
    val shifts: List<Shift>
)
