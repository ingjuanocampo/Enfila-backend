package com.ingjuanocampo.enfila.backend.config

import com.ingjuanocampo.enfila.backend.services.NotificationDeliveryWorker
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.ApplicationStopped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory

fun Application.configureNotificationWorker() {
    val worker by inject<NotificationDeliveryWorker>()
    val logger = LoggerFactory.getLogger("NotificationWorker")
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    environment.monitor.subscribe(ApplicationStarted) {
        scope.launch {
            while (isActive) {
                try {
                    worker.processOnce()
                } catch (e: Exception) {
                    logger.error("Turn-ready notification worker failed", e)
                }
                delay(5_000)
            }
        }
    }
    environment.monitor.subscribe(ApplicationStopped) {
        scope.cancel()
    }
}
