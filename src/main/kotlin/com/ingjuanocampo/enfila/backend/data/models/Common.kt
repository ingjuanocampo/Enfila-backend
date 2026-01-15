package com.ingjuanocampo.enfila.backend.data.models

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class PaginatedResponse<T>(
    val items: List<T>,
    val total: Int,
    val page: Int,
    val pageSize: Int,
    val hasNext: Boolean
)

@Serializable
data class ErrorResponse(
    val error: String,
    val code: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class BadRequestResponse(
    val error: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class MigrationResponse(
    val success: Boolean,
    val message: String? = null,
    val error: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

fun <T> T.toApiResponse(): ApiResponse<T> = ApiResponse(success = true, data = this)
fun <T> String.toErrorResponse(code: String? = null): ApiResponse<T> = 
    ApiResponse(success = false, error = this)

// Keep the original for backward compatibility
fun String.toErrorResponseNothing(code: String? = null): ApiResponse<Nothing> = 
    ApiResponse(success = false, error = this)
