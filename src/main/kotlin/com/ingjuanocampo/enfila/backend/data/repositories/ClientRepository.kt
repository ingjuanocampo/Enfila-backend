package com.ingjuanocampo.enfila.backend.data.repositories

import com.ingjuanocampo.enfila.backend.data.database.ClientsTable
import com.ingjuanocampo.enfila.backend.data.models.Client
import com.ingjuanocampo.enfila.backend.data.models.CreateClientRequest
import com.ingjuanocampo.enfila.backend.data.models.UpdateClientRequest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

interface ClientRepository {
    suspend fun create(request: CreateClientRequest): Client
    suspend fun getById(id: String): Client?
    suspend fun getAll(): List<Client>
    suspend fun update(id: String, request: UpdateClientRequest): Client?
    suspend fun delete(id: String): Boolean
    suspend fun addShiftToClient(clientId: String, shiftId: String): Boolean
    suspend fun removeShiftFromClient(clientId: String, shiftId: String): Boolean
}

class ClientRepositoryImpl : ClientRepository {
    
    private fun ResultRow.toClient(): Client = Client(
        id = this[ClientsTable.id].value,
        name = this[ClientsTable.name],
        shifts = this[ClientsTable.shifts]?.let { 
            Json.decodeFromString<List<String>>(it) 
        }
    )
    
    override suspend fun create(request: CreateClientRequest): Client = transaction {
        ClientsTable.insert {
            it[ClientsTable.id] = EntityID(request.id, ClientsTable)
            it[name] = request.name
            it[shifts] = Json.encodeToString(emptyList<String>())
        }
        
        Client(
            id = request.id,
            name = request.name,
            shifts = emptyList()
        )
    }
    
    override suspend fun getById(id: String): Client? = transaction {
        ClientsTable.select { ClientsTable.id eq EntityID(id, ClientsTable) }
            .map { it.toClient() }
            .singleOrNull()
    }
    
    override suspend fun getAll(): List<Client> = transaction {
        ClientsTable.selectAll()
            .map { it.toClient() }
    }
    
    override suspend fun update(id: String, request: UpdateClientRequest): Client? = newSuspendedTransaction {
        val exists = ClientsTable.select { ClientsTable.id eq EntityID(id, ClientsTable) }.count() > 0
        if (!exists) return@newSuspendedTransaction null
        
        ClientsTable.update({ ClientsTable.id eq EntityID(id, ClientsTable) }) {
            if (request.name != null) it[name] = request.name
            it[updatedAt] = Instant.now()
        }
        
        getById(id)
    }
    
    override suspend fun delete(id: String): Boolean = transaction {
        ClientsTable.deleteWhere { ClientsTable.id eq EntityID(id, ClientsTable) } > 0
    }
    
    override suspend fun addShiftToClient(clientId: String, shiftId: String): Boolean = newSuspendedTransaction {
        val client = getById(clientId) ?: return@newSuspendedTransaction false
        val currentShifts = client.shifts ?: emptyList()
        
        if (shiftId in currentShifts) return@newSuspendedTransaction true
        
        val updatedShifts = currentShifts + shiftId
        ClientsTable.update({ ClientsTable.id eq EntityID(clientId, ClientsTable) }) {
            it[shifts] = Json.encodeToString(updatedShifts)
            it[updatedAt] = Instant.now()
        }
        
        true
    }
    
    override suspend fun removeShiftFromClient(clientId: String, shiftId: String): Boolean = newSuspendedTransaction {
        val client = getById(clientId) ?: return@newSuspendedTransaction false
        val currentShifts = client.shifts ?: emptyList()
        
        val updatedShifts = currentShifts - shiftId
        ClientsTable.update({ ClientsTable.id eq EntityID(clientId, ClientsTable) }) {
            it[shifts] = Json.encodeToString(updatedShifts)
            it[updatedAt] = Instant.now()
        }
        
        true
    }
}
