package com.ingjuanocampo.enfila.backend.routes

import com.ingjuanocampo.enfila.backend.data.models.MigrationResponse
import com.ingjuanocampo.enfila.backend.services.MigrationService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.migrationRoutes() {
    val migrationService by inject<MigrationService>()

    route("/migration") {
        // Migrate data from Firebase
        post("/from-firebase") {
            try {
                migrationService.migrateFromFirebase()
                call.respond(HttpStatusCode.OK, MigrationResponse(
                    success = true,
                    message = "Migration completed successfully"
                )
                )
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, MigrationResponse(
                    success = false,
                    error = e.message ?: "Migration failed"
                ))
            }
        }

        // Check migration status
        get("/status") {
            val status = migrationService.getMigrationStatus()
            call.respond(status)
        }
    }
}
