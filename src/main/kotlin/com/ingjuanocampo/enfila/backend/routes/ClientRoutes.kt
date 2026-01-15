package com.ingjuanocampo.enfila.backend.routes

import com.ingjuanocampo.enfila.backend.data.models.BadRequestResponse
import com.ingjuanocampo.enfila.backend.data.models.CreateClientRequest
import com.ingjuanocampo.enfila.backend.data.models.UpdateClientRequest
import com.ingjuanocampo.enfila.backend.services.ClientService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.clientRoutes() {
    val clientService by inject<ClientService>()

    route("/clients") {
        // Create client
        post {
            val request = call.receive<CreateClientRequest>()
            val response = clientService.createClient(request)

            if (response.success) {
                call.respond(HttpStatusCode.Created, response)
            } else {
                call.respond(HttpStatusCode.BadRequest, response)
            }
        }

        // Get all clients
        get {
            val response = clientService.getAllClients()
            call.respond(response)
        }

        // Get client by ID
        get("/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                BadRequestResponse("ID parameter is required")
            )

            val response = clientService.getClient(id)
            if (response.success) {
                call.respond(response)
            } else {
                call.respond(HttpStatusCode.NotFound, response)
            }
        }

        // Update client
        put("/{id}") {
            val id = call.parameters["id"] ?: return@put call.respond(
                HttpStatusCode.BadRequest,
                BadRequestResponse("ID parameter is required")
            )

            val request = call.receive<UpdateClientRequest>()
            val response = clientService.updateClient(id, request)

            if (response.success) {
                call.respond(response)
            } else {
                call.respond(HttpStatusCode.NotFound, response)
            }
        }

        // Delete client
        delete("/{id}") {
            val id = call.parameters["id"] ?: return@delete call.respond(
                HttpStatusCode.BadRequest,
                BadRequestResponse("ID parameter is required")
            )

            val response = clientService.deleteClient(id)
            if (response.success) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound, response)
            }
        }
    }
}
