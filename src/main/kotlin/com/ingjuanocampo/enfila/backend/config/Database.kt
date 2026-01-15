package com.ingjuanocampo.enfila.backend.config

import com.ingjuanocampo.enfila.backend.data.database.*
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureDatabase() {
    // Use environment variables for database configuration (set in docker-compose.yml)
    val driver = "org.postgresql.Driver"
    val url = System.getenv("DATABASE_URL") ?: "jdbc:postgresql://localhost:5432/enfila_db"
    val user = System.getenv("DATABASE_USER") ?: "enfila_user"
    val password = System.getenv("DATABASE_PASSWORD") ?: "enfila_password"
    val maxPoolSize = System.getenv("DATABASE_MAX_POOL_SIZE")?.toInt() ?: 20
    
    println("Database configuration:")
    println("  URL: $url")
    println("  User: $user")
    println("  Max Pool Size: $maxPoolSize")
    
    val config = HikariConfig().apply {
        driverClassName = driver
        jdbcUrl = url
        username = user
        this.password = password
        maximumPoolSize = maxPoolSize
        isAutoCommit = false
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        
        validate()
    }
    
    val dataSource = HikariDataSource(config)
    Database.connect(dataSource)
    
    // Create tables
    transaction {
        SchemaUtils.create(
            UsersTable,
            ClientsTable,
            CompanySitesTable,
            ShiftsTable,
            ConfigTable
        )
    }
}
