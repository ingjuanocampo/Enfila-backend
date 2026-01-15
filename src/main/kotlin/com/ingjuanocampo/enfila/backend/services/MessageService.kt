package com.ingjuanocampo.enfila.backend.services

import com.ingjuanocampo.enfila.backend.data.models.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import java.util.*

interface MessageService {
    suspend fun sendMessage(request: SendMessageRequest): ApiResponse<MessageResponse>
    suspend fun getTwilioCredentials(): TwilioCredentials?
}

@Serializable
data class TwilioResponse(
    val sid: String? = null,
    val status: String? = null,
    val error_code: String? = null,
    val error_message: String? = null
)

class MessageServiceImpl : MessageService {
    
    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
        install(Logging) {
            level = LogLevel.INFO
        }
    }
    
    override suspend fun sendMessage(request: SendMessageRequest): ApiResponse<MessageResponse> {
        return try {
            val credentials = getTwilioCredentials()
            if (credentials == null) {
                return "Twilio credentials not configured".toErrorResponse()
            }
            
            val token = "${credentials.sid}:${credentials.token}"
            val tokenEncode = Base64.getEncoder().encodeToString(token.toByteArray())
            
            val response: TwilioResponse = httpClient.submitForm(
                url = "https://api.twilio.com/2010-04-01/Accounts/${credentials.sid}/Messages.json",
                formParameters = Parameters.build {
                    append("To", "whatsapp:+${request.to}")
                    append("From", "whatsapp:+${request.from}")
                    append("Body", request.body)
                }
            ) {
                header(HttpHeaders.Authorization, "Basic $tokenEncode")
            }.body()
            
            if (response.error_code != null) {
                MessageResponse(
                    success = false,
                    error = response.error_message ?: "Unknown error"
                ).toApiResponse()
            } else {
                MessageResponse(
                    success = true,
                    messageId = response.sid
                ).toApiResponse()
            }
            
        } catch (e: Exception) {
            MessageResponse(
                success = false,
                error = e.message ?: "Failed to send message"
            ).toApiResponse()
        }
    }
    
    override suspend fun getTwilioCredentials(): TwilioCredentials? {
        // In production, load from environment variables or secure configuration
        val sid = System.getenv("TWILIO_SID")
        val token = System.getenv("TWILIO_TOKEN")
        
        return if (sid != null && token != null) {
            TwilioCredentials(sid = sid, token = token)
        } else null
    }
}
