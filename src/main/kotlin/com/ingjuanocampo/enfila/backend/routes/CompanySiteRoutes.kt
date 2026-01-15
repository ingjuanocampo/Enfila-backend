package com.ingjuanocampo.enfila.backend.routes

import com.ingjuanocampo.enfila.backend.data.models.BadRequestResponse
import com.ingjuanocampo.enfila.backend.data.models.CreateCompanySiteRequest
import com.ingjuanocampo.enfila.backend.data.models.UpdateCompanySiteRequest
import com.ingjuanocampo.enfila.backend.services.CompanySiteService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.companySiteRoutes() {
    val companySiteService by inject<CompanySiteService>()

    route("/company-sites") {
        // Create company site
        post {
            val request = call.receive<CreateCompanySiteRequest>()
            val response = companySiteService.createCompanySite(request)

            if (response.success) {
                call.respond(HttpStatusCode.Created, response)
            } else {
                call.respond(HttpStatusCode.BadRequest, response)
            }
        }

        // Get all company sites
        get {
            val response = companySiteService.getAllCompanySites()
            call.respond(response)
        }

        // Get company site by ID
        get("/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                BadRequestResponse("ID parameter is required")
            )

            val response = companySiteService.getCompanySite(id)
            if (response.success) {
                call.respond(response)
            } else {
                call.respond(HttpStatusCode.NotFound, response)
            }
        }

        // Update company site
        put("/{id}") {
            val id = call.parameters["id"] ?: return@put call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "ID parameter is required")
            )

            val request = call.receive<UpdateCompanySiteRequest>()
            val response = companySiteService.updateCompanySite(id, request)

            if (response.success) {
                call.respond(response)
            } else {
                call.respond(HttpStatusCode.NotFound, response)
            }
        }

        // Delete company site
        delete("/{id}") {
            val id = call.parameters["id"] ?: return@delete call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "ID parameter is required")
            )

            val response = companySiteService.deleteCompanySite(id)
            if (response.success) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound, response)
            }
        }
    }
}
