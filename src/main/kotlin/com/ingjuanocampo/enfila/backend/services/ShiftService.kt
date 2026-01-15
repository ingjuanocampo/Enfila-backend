package com.ingjuanocampo.enfila.backend.services

import com.ingjuanocampo.enfila.backend.data.models.*
import com.ingjuanocampo.enfila.backend.data.repositories.ClientRepository
import com.ingjuanocampo.enfila.backend.data.repositories.ShiftRepository

interface ShiftService {
    suspend fun createShift(request: CreateShiftRequest): ApiResponse<Shift>
    suspend fun getShift(id: String): ApiResponse<Shift>
    suspend fun getShiftDetails(id: String): ApiResponse<ShiftDetails>
    suspend fun getAllShifts(): ApiResponse<List<Shift>>
    suspend fun getShiftsByCompanySite(companySiteId: String): ApiResponse<List<Shift>>
    suspend fun getShiftsByContact(contactId: String): ApiResponse<List<Shift>>
    suspend fun updateShift(id: String, request: UpdateShiftRequest): ApiResponse<Shift>
    suspend fun deleteShift(id: String): ApiResponse<Unit>
    suspend fun assignShift(companySiteId: String, contactId: String, notes: String?): ApiResponse<Shift>
}

class ShiftServiceImpl(
    private val shiftRepository: ShiftRepository,
    private val clientRepository: ClientRepository
) : ShiftService {
    
    override suspend fun createShift(request: CreateShiftRequest): ApiResponse<Shift> {
        return try {
            val shift = shiftRepository.create(request)
            
            // Add shift to client's shifts list
            clientRepository.addShiftToClient(request.contactId, shift.id)
            
            shift.toApiResponse()
        } catch (e: Exception) {
            "Failed to create shift: ${e.message}".toErrorResponse()
        }
    }
    
    override suspend fun getShift(id: String): ApiResponse<Shift> {
        return try {
            val shift = shiftRepository.getById(id)
            if (shift != null) {
                shift.toApiResponse()
            } else {
                "Shift not found".toErrorResponse()
            }
        } catch (e: Exception) {
            "Failed to get shift: ${e.message}".toErrorResponse()
        }
    }
    
    override suspend fun getShiftDetails(id: String): ApiResponse<ShiftDetails> {
        return try {
            val shift = shiftRepository.getById(id)
            if (shift != null) {
                val client = clientRepository.getById(shift.contactId)
                ShiftDetails(shift = shift, client = client).toApiResponse()
            } else {
                "Shift not found".toErrorResponse()
            }
        } catch (e: Exception) {
            "Failed to get shift details: ${e.message}".toErrorResponse()
        }
    }
    
    override suspend fun getAllShifts(): ApiResponse<List<Shift>> {
        return try {
            val shifts = shiftRepository.getAll()
            shifts.toApiResponse()
        } catch (e: Exception) {
            "Failed to get shifts: ${e.message}".toErrorResponse()
        }
    }
    
    override suspend fun getShiftsByCompanySite(companySiteId: String): ApiResponse<List<Shift>> {
        return try {
            val shifts = shiftRepository.getByCompanySite(companySiteId)
            shifts.toApiResponse()
        } catch (e: Exception) {
            "Failed to get shifts: ${e.message}".toErrorResponse()
        }
    }
    
    override suspend fun getShiftsByContact(contactId: String): ApiResponse<List<Shift>> {
        return try {
            val shifts = shiftRepository.getByContactId(contactId)
            shifts.toApiResponse()
        } catch (e: Exception) {
            "Failed to get shifts: ${e.message}".toErrorResponse()
        }
    }
    
    override suspend fun updateShift(id: String, request: UpdateShiftRequest): ApiResponse<Shift> {
        return try {
            val shift = shiftRepository.update(id, request)
            if (shift != null) {
                shift.toApiResponse()
            } else {
                "Shift not found".toErrorResponse()
            }
        } catch (e: Exception) {
            "Failed to update shift: ${e.message}".toErrorResponse()
        }
    }
    
    override suspend fun deleteShift(id: String): ApiResponse<Unit> {
        return try {
            val shift = shiftRepository.getById(id)
            if (shift != null) {
                // Remove shift from client's shifts list
                clientRepository.removeShiftFromClient(shift.contactId, id)
                
                val deleted = shiftRepository.delete(id)
                if (deleted) {
                    Unit.toApiResponse()
                } else {
                    "Failed to delete shift".toErrorResponse()
                }
            } else {
                "Shift not found".toErrorResponse()
            }
        } catch (e: Exception) {
            "Failed to delete shift: ${e.message}".toErrorResponse()
        }
    }
    
    override suspend fun assignShift(companySiteId: String, contactId: String, notes: String?): ApiResponse<Shift> {
        return try {
            // Get next shift number for the company site
            val nextNumber = shiftRepository.getNextNumber(companySiteId)
            
            // Create or get client
            var client = clientRepository.getById(contactId)
            if (client == null) {
                client = clientRepository.create(CreateClientRequest(id = contactId, name = contactId))
            }
            
            // Create shift request
            val request = CreateShiftRequest(
                parentCompanySite = companySiteId,
                number = nextNumber,
                contactId = contactId,
                notes = notes
            )
            
            createShift(request)
        } catch (e: Exception) {
            "Failed to assign shift: ${e.message}".toErrorResponse()
        }
    }
}
