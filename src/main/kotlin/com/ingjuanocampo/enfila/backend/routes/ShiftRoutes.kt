package com.ingjuanocampo.enfila.backend.routes

import com.ingjuanocampo.enfila.backend.data.models.BadRequestResponse
import com.ingjuanocampo.enfila.backend.data.models.CreateShiftRequest
import com.ingjuanocampo.enfila.backend.data.models.UpdateShiftRequest
import com.ingjuanocampo.enfila.backend.services.ShiftService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject

@Serializable
data class AssignShiftRequest(
    val companySiteId: String,
    val contactId: String,
    val notes: String? = null
)

fun Route.shiftRoutes() {
    val shiftService by inject<ShiftService>()

    route("/shifts") {
        // Create shift
        post {
            val request = call.receive<CreateShiftRequest>()
            val response = shiftService.createShift(request)

            if (response.success) {
                call.respond(HttpStatusCode.Created, response)
            } else {
                call.respond(HttpStatusCode.BadRequest, response)
            }
        }

        // Assign shift (simplified endpoint for the app)
        post("/assign") {
            val request = call.receive<AssignShiftRequest>()
            val response = shiftService.assignShift(
                companySiteId = request.companySiteId,
                contactId = request.contactId,
                notes = request.notes
            )

            if (response.success) {
                call.respond(HttpStatusCode.Created, response)
            } else {
                call.respond(HttpStatusCode.BadRequest, response)
            }
        }

        // Get all shifts
        get {
            val companySiteId = call.request.queryParameters["companySiteId"]
            val contactId = call.request.queryParameters["contactId"]

            val response = when {
                companySiteId != null -> shiftService.getShiftsByCompanySite(companySiteId)
                contactId != null -> shiftService.getShiftsByContact(contactId)
                else -> shiftService.getAllShifts()
            }

            call.respond(response)
        }

        // Get shift by ID
        get("/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                BadRequestResponse("ID parameter is required")
            )

            val response = shiftService.getShift(id)
            if (response.success) {
                call.respond(response)
            } else {
                call.respond(HttpStatusCode.NotFound, response)
            }
        }

        // Get shift details (includes client info)
        get("/{id}/details") {
            val id = call.parameters["id"] ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                BadRequestResponse("ID parameter is required")
            )

            val response = shiftService.getShiftDetails(id)
            if (response.success) {
                call.respond(response)
            } else {
                call.respond(HttpStatusCode.NotFound, response)
            }
        }

        // Update shift
        put("/{id}") {
            val id = call.parameters["id"] ?: return@put call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "ID parameter is required")
            )

            val request = call.receive<UpdateShiftRequest>()
            val response = shiftService.updateShift(id, request)

            if (response.success) {
                call.respond(response)
            } else {
                call.respond(HttpStatusCode.NotFound, response)
            }
        }

        // Delete shift
        delete("/{id}") {
            val id = call.parameters["id"] ?: return@delete call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "ID parameter is required")
            )

            val response = shiftService.deleteShift(id)
            if (response.success) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound, response)
            }
        }
    }
}
