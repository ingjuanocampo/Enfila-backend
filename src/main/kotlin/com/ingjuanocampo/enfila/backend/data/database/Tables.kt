package com.ingjuanocampo.enfila.backend.data.database

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

// Users table
object UsersTable : IdTable<String>("users") {
    override val id: Column<EntityID<String>> = varchar("id", 255).entityId()
    val phone = varchar("phone", 20)
    val name = varchar("name", 255).nullable()
    val companyIds = text("company_ids").nullable() // JSON array as text
    val createdAt = timestamp("created_at").default(Instant.now())
    val updatedAt = timestamp("updated_at").default(Instant.now())
    
    override val primaryKey = PrimaryKey(id)
}

// Clients table
object ClientsTable : IdTable<String>("clients") {
    override val id: Column<EntityID<String>> = varchar("id", 255).entityId()
    val name = varchar("name", 255).nullable()
    val shifts = text("shifts").nullable() // JSON array as text
    val createdAt = timestamp("created_at").default(Instant.now())
    val updatedAt = timestamp("updated_at").default(Instant.now())
    
    override val primaryKey = PrimaryKey(id)
}

// Company Sites table
object CompanySitesTable : IdTable<String>("company_sites") {
    override val id: Column<EntityID<String>> = varchar("id", 255).entityId()
    val name = varchar("name", 255).nullable()
    val shiftsIdList = text("shifts_id_list").nullable() // JSON array as text
    val createdAt = timestamp("created_at").default(Instant.now())
    val updatedAt = timestamp("updated_at").default(Instant.now())
    
    override val primaryKey = PrimaryKey(id)
}

// Shifts table
object ShiftsTable : IdTable<String>("shifts") {
    override val id: Column<EntityID<String>> = varchar("id", 255).entityId()
    val date = long("date")
    val parentCompanySite = varchar("parent_company_site", 255)
    val number = integer("number")
    val contactId = varchar("contact_id", 255)
    val notes = text("notes").nullable()
    val state = enumeration("state", com.ingjuanocampo.enfila.backend.data.models.ShiftState::class)
    val attentionStartDate = long("attention_start_date").nullable()
    val endDate = long("end_date").nullable()
    val createdAt = timestamp("created_at").default(Instant.now())
    val updatedAt = timestamp("updated_at").default(Instant.now())
    
    override val primaryKey = PrimaryKey(id)
}

// Tips table
object TipsTable : IdTable<String>("tips") {
    override val id: Column<EntityID<String>> = varchar("id", 36).entityId()
    val displayOrder = integer("display_order")
    val question = text("question")
    val answer = text("answer")
    val milestone = enumeration("milestone", com.ingjuanocampo.enfila.backend.data.models.TipMilestone::class)
    val published = bool("published").default(true)
    val createdAt = timestamp("created_at").default(Instant.now())
    val updatedAt = timestamp("updated_at").default(Instant.now())

    override val primaryKey = PrimaryKey(id)
}

// Configuration table for storing app settings
object ConfigTable : Table("config") {
    val key = varchar("key", 255)
    val value = text("value")
    val description = varchar("description", 500).nullable()
    val createdAt = timestamp("created_at").default(Instant.now())
    val updatedAt = timestamp("updated_at").default(Instant.now())
    
    override val primaryKey = PrimaryKey(key)
}
