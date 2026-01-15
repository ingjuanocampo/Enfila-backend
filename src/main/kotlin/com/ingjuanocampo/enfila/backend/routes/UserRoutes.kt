package com.ingjuanocampo.enfila.backend.routes

import com.ingjuanocampo.enfila.backend.data.models.BadRequestResponse
import com.ingjuanocampo.enfila.backend.data.models.CreateUserRequest
import com.ingjuanocampo.enfila.backend.data.models.ErrorResponse
import com.ingjuanocampo.enfila.backend.data.models.UpdateUserRequest
import com.ingjuanocampo.enfila.backend.services.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.userRoutes() {
    val userService by inject<UserService>()

    route("/users") {
        // Create user
        post {
            val request = call.receive<CreateUserRequest>()
            val response = userService.createUser(request)

            if (response.success) {
                call.respond(HttpStatusCode.Created, response)
            } else {
                call.respond(HttpStatusCode.BadRequest, response)
            }
        }

        // Get all users
        get {
            val response = userService.getAllUsers()
            call.respond(response)
        }

        // Get user by phone
        get("/by-phone/{phone}") {
            val phone = call.parameters["phone"] ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                BadRequestResponse("Phone parameter is required")
            )

            val response = userService.getUserByPhone(phone)
            if (response.success) {
                call.respond(response)
            } else {
                call.respond(HttpStatusCode.NotFound, response)
            }
        }

        // Get user by ID
        get("/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                BadRequestResponse("ID parameter is required")
            )

            val response = userService.getUser(id)
            if (response.success) {
                call.respond(response)
            } else {
                call.respond(HttpStatusCode.NotFound, ErrorResponse(
                    error = response.error.orEmpty()
                ))
            }
        }

        // Update user
        put("/{id}") {
            val id = call.parameters["id"] ?: return@put call.respond(
                HttpStatusCode.BadRequest,
                BadRequestResponse("ID parameter is required")
            )

            val request = call.receive<UpdateUserRequest>()
            val response = userService.updateUser(id, request)

            if (response.success) {
                call.respond(response)
            } else {
                call.respond(HttpStatusCode.NotFound, response)
            }
        }

        // Delete user
        delete("/{id}") {
            val id = call.parameters["id"] ?: return@delete call.respond(
                HttpStatusCode.BadRequest,
                BadRequestResponse("ID parameter is required")
            )

            val response = userService.deleteUser(id)
            if (response.success) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound, response)
            }
        }
    }
}
