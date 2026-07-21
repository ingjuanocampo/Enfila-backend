package com.ingjuanocampo.enfila.backend.data.repositories

import com.ingjuanocampo.enfila.backend.data.database.TipsTable
import com.ingjuanocampo.enfila.backend.data.models.PublishTipRequest
import com.ingjuanocampo.enfila.backend.data.models.Tip
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.util.UUID

interface TipRepository {
    suspend fun publish(request: PublishTipRequest): Tip
    suspend fun getPublishedOrdered(): List<Tip>
}

class TipRepositoryImpl : TipRepository {

    private fun ResultRow.toTip(): Tip = Tip(
        id = this[TipsTable.id].value,
        order = this[TipsTable.displayOrder],
        question = this[TipsTable.question],
        answer = this[TipsTable.answer],
        milestone = this[TipsTable.milestone],
        published = this[TipsTable.published],
    )

    override suspend fun publish(request: PublishTipRequest): Tip = transaction {
        val id = request.id ?: UUID.randomUUID().toString()
        val entityId = EntityID(id, TipsTable)
        val exists = TipsTable.select { TipsTable.id eq entityId }.count() > 0

        if (exists) {
            TipsTable.update({ TipsTable.id eq entityId }) {
                it[displayOrder] = request.order
                it[question] = request.question
                it[answer] = request.answer
                it[milestone] = request.milestone
                it[published] = request.published
                it[updatedAt] = Instant.now()
            }
        } else {
            TipsTable.insert {
                it[TipsTable.id] = entityId
                it[displayOrder] = request.order
                it[question] = request.question
                it[answer] = request.answer
                it[milestone] = request.milestone
                it[published] = request.published
            }
        }

        TipsTable.select { TipsTable.id eq entityId }
            .map { it.toTip() }
            .single()
    }

    override suspend fun getPublishedOrdered(): List<Tip> = transaction {
        TipsTable.select { TipsTable.published eq true }
            .orderBy(TipsTable.displayOrder to SortOrder.ASC)
            .map { it.toTip() }
    }
}
