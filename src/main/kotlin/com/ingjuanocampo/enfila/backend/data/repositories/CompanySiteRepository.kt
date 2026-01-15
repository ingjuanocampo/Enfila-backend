package com.ingjuanocampo.enfila.backend.data.repositories

import com.ingjuanocampo.enfila.backend.data.database.CompanySitesTable
import com.ingjuanocampo.enfila.backend.data.models.CompanySite
import com.ingjuanocampo.enfila.backend.data.models.CreateCompanySiteRequest
import com.ingjuanocampo.enfila.backend.data.models.UpdateCompanySiteRequest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.util.*

interface CompanySiteRepository {
    suspend fun create(request: CreateCompanySiteRequest): CompanySite
    suspend fun getById(id: String): CompanySite?
    suspend fun getAll(): List<CompanySite>
    suspend fun update(id: String, request: UpdateCompanySiteRequest): CompanySite?
    suspend fun delete(id: String): Boolean
    suspend fun addShiftToCompanySite(companySiteId: String, shiftId: String): Boolean
    suspend fun removeShiftFromCompanySite(companySiteId: String, shiftId: String): Boolean
}

class CompanySiteRepositoryImpl : CompanySiteRepository {
    
    private fun ResultRow.toCompanySite(): CompanySite = CompanySite(
        id = this[CompanySitesTable.id].value,
        name = this[CompanySitesTable.name],
        shiftsIdList = this[CompanySitesTable.shiftsIdList]?.let { 
            Json.decodeFromString<List<String>>(it) 
        }
    )
    
    override suspend fun create(request: CreateCompanySiteRequest): CompanySite = transaction {
        val id = UUID.randomUUID().toString()
        
        CompanySitesTable.insert {
            it[CompanySitesTable.id] = EntityID(id, CompanySitesTable)
            it[name] = request.name
            it[shiftsIdList] = Json.encodeToString(emptyList<String>())
        }
        
        CompanySite(
            id = id,
            name = request.name,
            shiftsIdList = emptyList()
        )
    }
    
    override suspend fun getById(id: String): CompanySite? = transaction {
        CompanySitesTable.select { CompanySitesTable.id eq EntityID(id, CompanySitesTable) }
            .map { it.toCompanySite() }
            .singleOrNull()
    }
    
    override suspend fun getAll(): List<CompanySite> = transaction {
        CompanySitesTable.selectAll()
            .map { it.toCompanySite() }
    }
    
    override suspend fun update(id: String, request: UpdateCompanySiteRequest): CompanySite? = newSuspendedTransaction {
        val exists = CompanySitesTable.select { CompanySitesTable.id eq EntityID(id, CompanySitesTable) }.count() > 0
        if (!exists) return@newSuspendedTransaction null
        
        CompanySitesTable.update({ CompanySitesTable.id eq EntityID(id, CompanySitesTable) }) {
            if (request.name != null) it[name] = request.name
            it[updatedAt] = Instant.now()
        }
        
        getById(id)
    }
    
    override suspend fun delete(id: String): Boolean = transaction {
        CompanySitesTable.deleteWhere { CompanySitesTable.id eq EntityID(id, CompanySitesTable) } > 0
    }
    
    override suspend fun addShiftToCompanySite(companySiteId: String, shiftId: String): Boolean = newSuspendedTransaction {
        val companySite = getById(companySiteId) ?: return@newSuspendedTransaction false
        val currentShifts = companySite.shiftsIdList ?: emptyList()
        
        if (shiftId in currentShifts) return@newSuspendedTransaction true
        
        val updatedShifts = currentShifts + shiftId
        CompanySitesTable.update({ CompanySitesTable.id eq EntityID(companySiteId, CompanySitesTable) }) {
            it[shiftsIdList] = Json.encodeToString(updatedShifts)
            it[updatedAt] = Instant.now()
        }
        
        true
    }
    
    override suspend fun removeShiftFromCompanySite(companySiteId: String, shiftId: String): Boolean = newSuspendedTransaction {
        val companySite = getById(companySiteId) ?: return@newSuspendedTransaction false
        val currentShifts = companySite.shiftsIdList ?: emptyList()
        
        val updatedShifts = currentShifts - shiftId
        CompanySitesTable.update({ CompanySitesTable.id eq EntityID(companySiteId, CompanySitesTable) }) {
            it[shiftsIdList] = Json.encodeToString(updatedShifts)
            it[updatedAt] = Instant.now()
        }
        
        true
    }
}
