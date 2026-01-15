package com.ingjuanocampo.enfila.backend.data.repositories

import com.ingjuanocampo.enfila.backend.data.database.UsersTable
import com.ingjuanocampo.enfila.backend.data.models.CreateUserRequest
import com.ingjuanocampo.enfila.backend.data.models.UpdateUserRequest
import com.ingjuanocampo.enfila.backend.data.models.User
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

interface UserRepository {
    suspend fun create(request: CreateUserRequest): User
    suspend fun getById(id: String): User?
    suspend fun getByPhone(phone: String): User?
    suspend fun getAll(): List<User>
    suspend fun update(id: String, request: UpdateUserRequest): User?
    suspend fun delete(id: String): Boolean
}

class UserRepositoryImpl : UserRepository {

    private fun ResultRow.toUser(): User = User(
        id = this[UsersTable.id].value,
        phone = this[UsersTable.phone],
        name = this[UsersTable.name],
        companyIds = this[UsersTable.companyIds]?.let {
            Json.decodeFromString<List<String>>(it)
        }
    )

    override suspend fun create(request: CreateUserRequest): User = transaction {
        val id = request.id // Use google ID as ID

        UsersTable.insert {
            it[UsersTable.id] = EntityID(id, UsersTable)
            it[phone] = request.phone
            it[name] = request.name
            it[companyIds] = request.companyIds?.let { list -> Json.encodeToString(list) }
        }

        User(
            id = id,
            phone = request.phone,
            name = request.name,
            companyIds = request.companyIds
        )
    }

    override suspend fun getById(id: String): User? = transaction {
        UsersTable.select { UsersTable.id eq EntityID(id, UsersTable) }
            .map { it.toUser() }
            .singleOrNull()
    }

    override suspend fun getByPhone(phone: String): User? = transaction {
        UsersTable.select { UsersTable.phone eq phone }
            .map { it.toUser() }
            .singleOrNull()
    }

    override suspend fun getAll(): List<User> = transaction {
        UsersTable.selectAll()
            .map { it.toUser() }
    }

    override suspend fun update(id: String, request: UpdateUserRequest): User? = newSuspendedTransaction {
        val exists = UsersTable.select { UsersTable.id eq EntityID(id, UsersTable) }.count() > 0
        if (!exists) return@newSuspendedTransaction null

        UsersTable.update({ UsersTable.id eq EntityID(id, UsersTable) }) {
            if (request.name != null) it[name] = request.name
            if (request.companyIds != null) it[companyIds] = Json.encodeToString(request.companyIds)
            it[updatedAt] = Instant.now()
        }

        getById(id)
    }

    override suspend fun delete(id: String): Boolean = transaction {
        UsersTable.deleteWhere { UsersTable.id eq EntityID(id, UsersTable) } > 0
    }
}
