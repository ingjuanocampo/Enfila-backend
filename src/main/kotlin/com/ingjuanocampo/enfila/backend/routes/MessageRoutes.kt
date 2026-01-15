package com.ingjuanocampo.enfila.backend.routes

import com.ingjuanocampo.enfila.backend.data.models.SendMessageRequest
import com.ingjuanocampo.enfila.backend.services.MessageService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.messageRoutes() {
    val messageService by inject<MessageService>()
    
    route("/messages") {
        // Send message
        post("/send") {
            val request = call.receive<SendMessageRequest>()
            val response = messageService.sendMessage(request)
            
            if (response.success) {
                call.respond(HttpStatusCode.OK, response)
            } else {
                call.respond(HttpStatusCode.BadRequest, response)
            }
        }
    }
}
