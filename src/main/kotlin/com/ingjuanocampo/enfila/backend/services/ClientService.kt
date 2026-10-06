package com.ingjuanocampo.enfila.backend.services

import com.ingjuanocampo.enfila.backend.data.models.*
import com.ingjuanocampo.enfila.backend.data.repositories.ClientRepository
import com.ingjuanocampo.enfila.backend.data.repositories.CompanySiteRepository
import java.time.LocalDate

interface ClientService {
    suspend fun createClient(request: CreateClientRequest): ApiResponse<Client>
    suspend fun getClient(id: String): ApiResponse<Client>
    suspend fun getAllClients(): ApiResponse<List<Client>>
    suspend fun updateClient(id: String, request: UpdateClientRequest): ApiResponse<Client>
    suspend fun deleteClient(id: String): ApiResponse<Unit>
}

class ClientServiceImpl(
    private val clientRepository: ClientRepository,
    private val companySiteRepository: CompanySiteRepository,
) : ClientService {

    private val emailPattern = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val sexes = setOf("FEMALE", "MALE", "OTHER")
    
    override suspend fun createClient(request: CreateClientRequest): ApiResponse<Client> {
        return try {
            // Check if client already exists
            val existing = clientRepository.getById(request.id)
            if (existing != null) {
                return "Client with ID ${request.id} already exists".toErrorResponse()
            }
            
            val client = clientRepository.create(request)
            client.toApiResponse()
        } catch (e: Exception) {
            "Failed to create client: ${e.message}".toErrorResponse()
        }
    }
    
    override suspend fun getClient(id: String): ApiResponse<Client> {
        return try {
            val client = clientRepository.getById(id)
            if (client != null) {
                client.toApiResponse()
            } else {
                "Client not found".toErrorResponse()
            }
        } catch (e: Exception) {
            "Failed to get client: ${e.message}".toErrorResponse()
        }
    }
    
    override suspend fun getAllClients(): ApiResponse<List<Client>> {
        return try {
            val clients = clientRepository.getAll()
            clients.toApiResponse()
        } catch (e: Exception) {
            "Failed to get clients: ${e.message}".toErrorResponse()
        }
    }
    
    override suspend fun updateClient(id: String, request: UpdateClientRequest): ApiResponse<Client> {
        return try {
            val normalized = request.normalized()
            validate(normalized)?.let { return it.toErrorResponse() }
            val storeId = normalized.favoriteStoreId
            if (!storeId.isNullOrEmpty() && companySiteRepository.getById(storeId) == null) {
                return "Favorite store not found".toErrorResponse()
            }
            val client = clientRepository.update(id, normalized)
            if (client != null) {
                client.toApiResponse()
            } else {
                "Client not found".toErrorResponse()
            }
        } catch (e: Exception) {
            "Failed to update client: ${e.message}".toErrorResponse()
        }
    }

    private fun UpdateClientRequest.normalized() = copy(
        name = name?.trim(),
        email = email?.trim(),
        birthDate = birthDate?.trim(),
        sex = sex?.trim()?.uppercase(),
        city = city?.trim(),
        notes = notes?.trim(),
        favoriteOrder = favoriteOrder?.trim(),
        favoriteStoreId = favoriteStoreId?.trim(),
    )

    private fun validate(request: UpdateClientRequest): String? {
        request.name?.let { if (it.length > 255) return "Name is too long" }
        request.email?.takeIf { it.isNotEmpty() }?.let { email ->
            if (email.length > 255 || !emailPattern.matches(email)) return "Invalid email"
        }
        request.birthDate?.takeIf { it.isNotEmpty() }?.let { raw ->
            val date = runCatching { LocalDate.parse(raw) }.getOrNull() ?: return "Invalid birth date"
            if (date.year < 1900 || date.isAfter(LocalDate.now())) return "Invalid birth date"
        }
        request.sex?.takeIf { it.isNotEmpty() }?.let { sex ->
            if (sex !in sexes) return "Invalid sex"
        }
        request.city?.let { if (it.length > 100) return "City is too long" }
        request.notes?.let { if (it.length > 500) return "Notes are too long" }
        request.favoriteOrder?.let { if (it.length > 120) return "Favorite order is too long" }
        return null
    }
    
    override suspend fun deleteClient(id: String): ApiResponse<Unit> {
        return try {
            val deleted = clientRepository.delete(id)
            if (deleted) {
                Unit.toApiResponse()
            } else {
                "Client not found".toErrorResponse()
            }
        } catch (e: Exception) {
            "Failed to delete client: ${e.message}".toErrorResponse()
        }
    }
}
