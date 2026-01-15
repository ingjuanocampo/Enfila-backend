package com.ingjuanocampo.enfila.backend

import com.ingjuanocampo.enfila.backend.config.configureDatabase
import com.ingjuanocampo.enfila.backend.config.configureDependencyInjection
import com.ingjuanocampo.enfila.backend.config.configureFirebase
import com.ingjuanocampo.enfila.backend.config.configureMigration
import com.ingjuanocampo.enfila.backend.config.configureRouting
import com.ingjuanocampo.enfila.backend.config.configureSecurity
import com.ingjuanocampo.enfila.backend.config.configureSerialization
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureDependencyInjection()
    configureDatabase()
    configureFirebase()
    configureSecurity()
    configureSerialization()
    configureMigration()
    configureRouting()
}
