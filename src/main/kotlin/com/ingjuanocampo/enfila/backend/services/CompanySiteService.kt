package com.ingjuanocampo.enfila.backend.services

import com.ingjuanocampo.enfila.backend.data.models.*
import com.ingjuanocampo.enfila.backend.data.repositories.CompanySiteRepository

interface CompanySiteService {
    suspend fun createCompanySite(request: CreateCompanySiteRequest): ApiResponse<CompanySite>
    suspend fun getCompanySite(id: String): ApiResponse<CompanySite>
    suspend fun getAllCompanySites(): ApiResponse<List<CompanySite>>
    suspend fun updateCompanySite(id: String, request: UpdateCompanySiteRequest): ApiResponse<CompanySite>
    suspend fun deleteCompanySite(id: String): ApiResponse<Unit>
}

class CompanySiteServiceImpl(
    private val companySiteRepository: CompanySiteRepository
) : CompanySiteService {
    
    override suspend fun createCompanySite(request: CreateCompanySiteRequest): ApiResponse<CompanySite> {
        return try {
            val companySite = companySiteRepository.create(request)
            companySite.toApiResponse()
        } catch (e: Exception) {
            "Failed to create company site: ${e.message}".toErrorResponse()
        }
    }
    
    override suspend fun getCompanySite(id: String): ApiResponse<CompanySite> {
        return try {
            val companySite = companySiteRepository.getById(id)
            if (companySite != null) {
                companySite.toApiResponse()
            } else {
                "Company site not found".toErrorResponse()
            }
        } catch (e: Exception) {
            "Failed to get company site: ${e.message}".toErrorResponse()
        }
    }
    
    override suspend fun getAllCompanySites(): ApiResponse<List<CompanySite>> {
        return try {
            val companySites = companySiteRepository.getAll()
            companySites.toApiResponse()
        } catch (e: Exception) {
            "Failed to get company sites: ${e.message}".toErrorResponse()
        }
    }
    
    override suspend fun updateCompanySite(id: String, request: UpdateCompanySiteRequest): ApiResponse<CompanySite> {
        return try {
            val companySite = companySiteRepository.update(id, request)
            if (companySite != null) {
                companySite.toApiResponse()
            } else {
                "Company site not found".toErrorResponse()
            }
        } catch (e: Exception) {
            "Failed to update company site: ${e.message}".toErrorResponse()
        }
    }
    
    override suspend fun deleteCompanySite(id: String): ApiResponse<Unit> {
        return try {
            val deleted = companySiteRepository.delete(id)
            if (deleted) {
                Unit.toApiResponse()
            } else {
                "Company site not found".toErrorResponse()
            }
        } catch (e: Exception) {
            "Failed to delete company site: ${e.message}".toErrorResponse()
        }
    }
}
