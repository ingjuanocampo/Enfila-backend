package com.ingjuanocampo.enfila.backend.support

import com.ingjuanocampo.enfila.backend.data.models.Client
import com.ingjuanocampo.enfila.backend.data.models.CompanySite
import com.ingjuanocampo.enfila.backend.data.models.CreateClientRequest
import com.ingjuanocampo.enfila.backend.data.models.CreateCompanySiteRequest
import com.ingjuanocampo.enfila.backend.data.models.CreateShiftRequest
import com.ingjuanocampo.enfila.backend.data.models.Shift
import com.ingjuanocampo.enfila.backend.data.models.UpdateClientRequest
import com.ingjuanocampo.enfila.backend.data.models.UpdateCompanySiteRequest
import com.ingjuanocampo.enfila.backend.data.models.UpdateShiftRequest
import com.ingjuanocampo.enfila.backend.data.repositories.ClientRepository
import com.ingjuanocampo.enfila.backend.data.repositories.CompanySiteRepository
import com.ingjuanocampo.enfila.backend.data.repositories.ShiftRepository

class FakeShiftRepository : ShiftRepository {
    val shifts = mutableMapOf<String, Shift>()

    override suspend fun create(request: CreateShiftRequest): Shift = error("unused")
    override suspend fun getById(id: String): Shift? = shifts[id]
    override suspend fun getAll(): List<Shift> = error("unused")
    override suspend fun getByCompanySite(companySiteId: String): List<Shift> = error("unused")
    override suspend fun getByContactId(contactId: String): List<Shift> = error("unused")
    override suspend fun delete(id: String): Boolean = error("unused")
    override suspend fun getNextNumber(companySiteId: String): Int = error("unused")

    override suspend fun update(id: String, request: UpdateShiftRequest): Shift? {
        val current = shifts[id] ?: return null
        val updated = current.copy(
            number = request.number ?: current.number,
            notes = request.notes ?: current.notes,
            state = request.state ?: current.state,
            attentionStartDate = request.attentionStartDate ?: current.attentionStartDate,
            endDate = request.endDate ?: current.endDate,
        )
        shifts[id] = updated
        return updated
    }
}

class FakeClientRepository : ClientRepository {
    override suspend fun create(request: CreateClientRequest): Client = error("unused")
    override suspend fun getById(id: String): Client? = error("unused")
    override suspend fun getAll(): List<Client> = error("unused")
    override suspend fun update(id: String, request: UpdateClientRequest): Client? = error("unused")
    override suspend fun delete(id: String): Boolean = error("unused")
    override suspend fun addShiftToClient(clientId: String, shiftId: String): Boolean = error("unused")
    override suspend fun removeShiftFromClient(clientId: String, shiftId: String): Boolean = error("unused")
}

class FakeCompanySiteRepository(
    private val sites: Map<String, CompanySite> = emptyMap(),
) : CompanySiteRepository {
    override suspend fun create(request: CreateCompanySiteRequest): CompanySite = error("unused")
    override suspend fun getById(id: String): CompanySite? = sites[id]
    override suspend fun getAll(): List<CompanySite> = error("unused")
    override suspend fun update(id: String, request: UpdateCompanySiteRequest): CompanySite? = error("unused")
    override suspend fun delete(id: String): Boolean = error("unused")
    override suspend fun addShiftToCompanySite(companySiteId: String, shiftId: String): Boolean = error("unused")
    override suspend fun removeShiftFromCompanySite(companySiteId: String, shiftId: String): Boolean = error("unused")
}
