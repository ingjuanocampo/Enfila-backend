package com.ingjuanocampo.enfila.backend.services

import com.ingjuanocampo.enfila.backend.data.models.*
import com.ingjuanocampo.enfila.backend.data.repositories.UserRepository

interface UserService {
    suspend fun createUser(request: CreateUserRequest): ApiResponse<User>
    suspend fun getUser(id: String): ApiResponse<User>
    suspend fun getUserByPhone(phone: String): ApiResponse<User>
    suspend fun getAllUsers(): ApiResponse<List<User>>
    suspend fun updateUser(id: String, request: UpdateUserRequest): ApiResponse<User>
    suspend fun deleteUser(id: String): ApiResponse<Unit>
}

class UserServiceImpl(
    private val userRepository: UserRepository
) : UserService {

    override suspend fun createUser(request: CreateUserRequest): ApiResponse<User> {
        return try {
            // Check if user already exists
            val existing = userRepository.getById(request.id)
            if (existing != null) {
                return "User with phone ${request.phone} already exists".toErrorResponse<User>()
            }

            val user = userRepository.create(request)
            user.toApiResponse()
        } catch (e: Exception) {
            "Failed to create user: ${e.message}".toErrorResponse<User>()
        }
    }

    override suspend fun getUser(id: String): ApiResponse<User> {
        return try {
            val user = userRepository.getById(id)
            if (user != null) {
                user.toApiResponse()
            } else {
                "User not found".toErrorResponse<User>()
            }
        } catch (e: Exception) {
            "Failed to get user: ${e.message}".toErrorResponse<User>()
        }
    }

    override suspend fun getUserByPhone(phone: String): ApiResponse<User> {
        return try {
            val user = userRepository.getByPhone(phone)
            if (user != null) {
                user.toApiResponse()
            } else {
                "User not found".toErrorResponse<User>()
            }
        } catch (e: Exception) {
            "Failed to get user: ${e.message}".toErrorResponse<User>()
        }
    }

    override suspend fun getAllUsers(): ApiResponse<List<User>> {
        return try {
            val users = userRepository.getAll()
            users.toApiResponse()
        } catch (e: Exception) {
            "Failed to get users: ${e.message}".toErrorResponse<List<User>>()
        }
    }

    override suspend fun updateUser(id: String, request: UpdateUserRequest): ApiResponse<User> {
        return try {
            val user = userRepository.update(id, request)
            if (user != null) {
                user.toApiResponse()
            } else {
                "User not found".toErrorResponse<User>()
            }
        } catch (e: Exception) {
            "Failed to update user: ${e.message}".toErrorResponse<User>()
        }
    }

    override suspend fun deleteUser(id: String): ApiResponse<Unit> {
        return try {
            val deleted = userRepository.delete(id)
            if (deleted) {
                Unit.toApiResponse()
            } else {
                "User not found".toErrorResponse<Unit>()
            }
        } catch (e: Exception) {
            "Failed to delete user: ${e.message}".toErrorResponse<Unit>()
        }
    }
}
