package com.ingjuanocampo.enfila.backend.services

import com.ingjuanocampo.enfila.backend.data.models.*
import com.ingjuanocampo.enfila.backend.data.repositories.ClientRepository

interface ClientService {
    suspend fun createClient(request: CreateClientRequest): ApiResponse<Client>
    suspend fun getClient(id: String): ApiResponse<Client>
    suspend fun getAllClients(): ApiResponse<List<Client>>
    suspend fun updateClient(id: String, request: UpdateClientRequest): ApiResponse<Client>
    suspend fun deleteClient(id: String): ApiResponse<Unit>
}

class ClientServiceImpl(
    private val clientRepository: ClientRepository
) : ClientService {
    
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
            val client = clientRepository.update(id, request)
            if (client != null) {
                client.toApiResponse()
            } else {
                "Client not found".toErrorResponse()
            }
        } catch (e: Exception) {
            "Failed to update client: ${e.message}".toErrorResponse()
        }
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
