package com.ingjuanocampo.enfila.backend.data.repositories

import com.ingjuanocampo.enfila.backend.data.database.ShiftsTable
import com.ingjuanocampo.enfila.backend.data.models.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.util.*

interface ShiftRepository {
    suspend fun create(request: CreateShiftRequest): Shift
    suspend fun getById(id: String): Shift?
    suspend fun getAll(): List<Shift>
    suspend fun getByCompanySite(companySiteId: String): List<Shift>
    suspend fun getByContactId(contactId: String): List<Shift>
    suspend fun update(id: String, request: UpdateShiftRequest): Shift?
    suspend fun delete(id: String): Boolean
    suspend fun getNextNumber(companySiteId: String): Int
}

class ShiftRepositoryImpl : ShiftRepository {
    
    private fun ResultRow.toShift(): Shift = Shift(
        id = this[ShiftsTable.id].value,
        date = this[ShiftsTable.date],
        parentCompanySite = this[ShiftsTable.parentCompanySite],
        number = this[ShiftsTable.number],
        contactId = this[ShiftsTable.contactId],
        notes = this[ShiftsTable.notes],
        state = this[ShiftsTable.state],
        attentionStartDate = this[ShiftsTable.attentionStartDate],
        endDate = this[ShiftsTable.endDate]
    )
    
    override suspend fun create(request: CreateShiftRequest): Shift = transaction {
        val id = "${request.number}${request.parentCompanySite}"
        val now = System.currentTimeMillis()
        
        ShiftsTable.insert {
            it[ShiftsTable.id] = EntityID(id, ShiftsTable)
            it[date] = now
            it[parentCompanySite] = request.parentCompanySite
            it[number] = request.number
            it[contactId] = request.contactId
            it[notes] = request.notes
            it[state] = ShiftState.WAITING
        }
        
        Shift(
            id = id,
            date = now,
            parentCompanySite = request.parentCompanySite,
            number = request.number,
            contactId = request.contactId,
            notes = request.notes,
            state = ShiftState.WAITING
        )
    }
    
    override suspend fun getById(id: String): Shift? = transaction {
        ShiftsTable.select { ShiftsTable.id eq EntityID(id, ShiftsTable) }
            .map { it.toShift() }
            .singleOrNull()
    }
    
    override suspend fun getAll(): List<Shift> = transaction {
        ShiftsTable.selectAll()
            .orderBy(ShiftsTable.date to SortOrder.DESC)
            .map { it.toShift() }
    }
    
    override suspend fun getByCompanySite(companySiteId: String): List<Shift> = transaction {
        ShiftsTable.select { ShiftsTable.parentCompanySite eq companySiteId }
            .orderBy(ShiftsTable.number to SortOrder.ASC)
            .map { it.toShift() }
    }
    
    override suspend fun getByContactId(contactId: String): List<Shift> = transaction {
        ShiftsTable.select { ShiftsTable.contactId eq contactId }
            .orderBy(ShiftsTable.date to SortOrder.DESC)
            .map { it.toShift() }
    }
    
    override suspend fun update(id: String, request: UpdateShiftRequest): Shift? = newSuspendedTransaction {
        val exists = ShiftsTable.select { ShiftsTable.id eq EntityID(id, ShiftsTable) }.count() > 0
        if (!exists) return@newSuspendedTransaction null
        
        ShiftsTable.update({ ShiftsTable.id eq EntityID(id, ShiftsTable) }) {
            if (request.number != null) it[number] = request.number
            if (request.notes != null) it[notes] = request.notes
            if (request.state != null) it[state] = request.state
            if (request.attentionStartDate != null) it[attentionStartDate] = request.attentionStartDate
            if (request.endDate != null) it[endDate] = request.endDate
            it[updatedAt] = Instant.now()
        }
        
        getById(id)
    }
    
    override suspend fun delete(id: String): Boolean = transaction {
        ShiftsTable.deleteWhere { ShiftsTable.id eq EntityID(id, ShiftsTable) } > 0
    }
    
    override suspend fun getNextNumber(companySiteId: String): Int = transaction {
        val maxNumber = ShiftsTable.select { ShiftsTable.parentCompanySite eq companySiteId }
            .maxOfOrNull { it[ShiftsTable.number] }
        
        (maxNumber ?: 0) + 1
    }
}
