package com.ingjuanocampo.enfila.backend.routes

import com.ingjuanocampo.enfila.backend.data.models.BadRequestResponse
import com.ingjuanocampo.enfila.backend.data.models.PublishTipRequest
import com.ingjuanocampo.enfila.backend.services.TipService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.tipRoutes() {
    val tipService by inject<TipService>()

    route("/tips") {
        post("/publish") {
            val request = call.receive<PublishTipRequest>()
            val response = tipService.publishTip(request)

            if (response.success) {
                call.respond(HttpStatusCode.Created, response)
            } else {
                call.respond(HttpStatusCode.BadRequest, response)
            }
        }

        get {
            val response = tipService.getPublishedTips()
            call.respond(response)
        }

        get("/for-user/{userId}") {
            val userId = call.parameters["userId"] ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                BadRequestResponse("User ID parameter is required"),
            )

            val response = tipService.getTipsForUser(userId)
            call.respond(response)
        }
    }
}
