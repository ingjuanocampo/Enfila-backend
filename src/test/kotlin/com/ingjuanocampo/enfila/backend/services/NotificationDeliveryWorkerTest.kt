package com.ingjuanocampo.enfila.backend.services

import com.ingjuanocampo.enfila.backend.data.models.MessageSendResult
import com.ingjuanocampo.enfila.backend.data.models.NotificationEventType
import com.ingjuanocampo.enfila.backend.data.models.NotificationInsert
import com.ingjuanocampo.enfila.backend.data.models.NotificationStatus
import com.ingjuanocampo.enfila.backend.support.InMemoryNotificationRepository
import kotlinx.coroutines.runBlocking
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class NotificationDeliveryWorkerTest {
    @Test
    fun retriesUntilTheSendSucceeds() = runBlocking {
        var clock = Instant.parse("2026-01-01T00:00:00Z")
        val repository = InMemoryNotificationRepository(clock = { clock })
        repository.insertIfAbsent(insert("n1"))
        val transport = ScriptedTransport(
            MessageSendResult(success = false, error = "down"),
            MessageSendResult(success = true, messageId = "SM123"),
        )
        val worker = NotificationDeliveryWorker(repository, transport, clock = { clock })

        worker.processOnce()
        val failed = repository.rows.single()
        assertEquals(NotificationStatus.FAILED, failed.status)
        assertEquals(1, failed.attempts)
        assertEquals(clock.plusSeconds(15), failed.nextAttemptAt)

        worker.processOnce()
        assertEquals(1, transport.calls)

        clock = failed.nextAttemptAt
        worker.processOnce()
        val sent = repository.rows.single()
        assertEquals(NotificationStatus.SENT, sent.status)
        assertEquals("SM123", sent.providerMessageId)
        assertEquals(2, transport.calls)
    }

    @Test
    fun stopsAfterFiveFailures() = runBlocking {
        var clock = Instant.EPOCH
        val repository = InMemoryNotificationRepository(clock = { clock })
        repository.insertIfAbsent(insert("n1"))
        val transport = ScriptedTransport(
            *(1..6).map { MessageSendResult(success = false, error = "down") }.toTypedArray(),
        )
        val worker = NotificationDeliveryWorker(repository, transport, clock = { clock })

        repeat(5) {
            worker.processOnce()
            clock = repository.rows.single().nextAttemptAt
        }

        assertEquals(5, transport.calls)
        assertEquals(5, repository.rows.single().attempts)
        worker.processOnce()
        assertEquals(5, transport.calls)
    }

    @Test
    fun duplicateInsertForTheSameCallIsIgnored() = runBlocking {
        val repository = InMemoryNotificationRepository()
        val first = insert("n1")
        assertEquals(true, repository.insertIfAbsent(first))
        assertEquals(false, repository.insertIfAbsent(first.copy(id = "n2")))
        assertEquals(1, repository.rows.size)
    }

    private fun insert(id: String) = NotificationInsert(
        id = id,
        shiftId = "s1",
        contactId = "573001112233",
        eventType = NotificationEventType.TURN_READY,
        body = "Es tu turno #4 en Cafe. Acércate por favor.",
        contentVariables = """{"1":"4","2":"Cafe"}""",
        attentionStartDate = 1_000L,
    )

    private class ScriptedTransport(vararg results: MessageSendResult) : TurnReadyTransport {
        private val remaining = results.toMutableList()
        var calls: Int = 0

        override suspend fun sendTurnReady(
            toDigits: String,
            body: String,
            contentVariablesJson: String?,
        ): MessageSendResult {
            calls += 1
            return remaining.removeAt(0)
        }
    }
}
