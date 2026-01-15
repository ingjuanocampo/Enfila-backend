package com.ingjuanocampo.enfila.backend.config

import com.ingjuanocampo.enfila.backend.services.MigrationService
import io.ktor.server.application.*
import kotlinx.coroutines.launch
import org.koin.ktor.ext.inject

fun Application.configureMigration() {
    val migrationService by inject<MigrationService>()
    
    // Optional: Run migration on startup
    val runMigrationOnStartup = environment.config.propertyOrNull("migration.onStartup")?.getString()?.toBoolean() ?: false
    
    if (runMigrationOnStartup) {
        launch {
            try {
                log.info("Running migration on startup...")
                migrationService.migrateFromFirebase()
                log.info("Migration completed successfully")
            } catch (e: Exception) {
                log.error("Migration failed", e)
            }
        }
    }
}
