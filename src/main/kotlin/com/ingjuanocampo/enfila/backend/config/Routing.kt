package com.ingjuanocampo.enfila.backend.config

import com.ingjuanocampo.enfila.backend.routes.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class HealthResponse(
    val status: String,
    val timestamp: Long
)

fun Application.configureRouting() {
    routing {
        // Health check
        get("/health") {
            call.respond(HealthResponse("OK", System.currentTimeMillis()))
        }
        
        // API routes
        route("/api/v1") {
            userRoutes()
            clientRoutes()
            shiftRoutes()
            companySiteRoutes()
            messageRoutes()
            migrationRoutes()
            tipRoutes()
        }
    }
}
