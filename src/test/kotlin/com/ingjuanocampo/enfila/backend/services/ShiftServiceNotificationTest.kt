package com.ingjuanocampo.enfila.backend.services

import com.ingjuanocampo.enfila.backend.data.models.CompanySite
import com.ingjuanocampo.enfila.backend.data.models.NotificationStatus
import com.ingjuanocampo.enfila.backend.data.models.Shift
import com.ingjuanocampo.enfila.backend.data.models.ShiftState
import com.ingjuanocampo.enfila.backend.data.models.UpdateShiftRequest
import com.ingjuanocampo.enfila.backend.support.FakeClientRepository
import com.ingjuanocampo.enfila.backend.support.FakeCompanySiteRepository
import com.ingjuanocampo.enfila.backend.support.FakeShiftRepository
import com.ingjuanocampo.enfila.backend.support.InMemoryNotificationRepository
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ShiftServiceNotificationTest {
    @Test
    fun transitionIntoCallingEnqueuesOnce() = runBlocking {
        val shifts = FakeShiftRepository()
        shifts.shifts["s1"] = waitingShift()
        val notifications = InMemoryNotificationRepository()
        val service = service(shifts, notifications, now = 1_000L)

        val updated = service.updateShift("s1", UpdateShiftRequest(state = ShiftState.CALLING))
        assertTrue(updated.success)
        assertEquals(ShiftState.CALLING, shifts.shifts["s1"]?.state)
        assertEquals(1_000L, shifts.shifts["s1"]?.attentionStartDate)

        val queued = notifications.rows.single()
        assertEquals(NotificationStatus.PENDING, queued.status)
        assertEquals("573001112233", queued.contactId)
        assertEquals("Es tu turno #4 en Cafe. Acércate por favor.", queued.body)
        assertEquals(1_000L, queued.attentionStartDate)

        service.updateShift("s1", UpdateShiftRequest(state = ShiftState.CALLING))
        assertEquals(1, notifications.rows.size)
    }

    @Test
    fun reenteringCallingQueuesAnotherMessage() = runBlocking {
        val shifts = FakeShiftRepository()
        shifts.shifts["s1"] = waitingShift()
        val notifications = InMemoryNotificationRepository()
        val service = service(shifts, notifications, now = 1_000L)

        service.updateShift("s1", UpdateShiftRequest(state = ShiftState.CALLING))
        shifts.shifts["s1"] = shifts.shifts.getValue("s1").copy(state = ShiftState.FINISHED)
        service.updateShift("s1", UpdateShiftRequest(state = ShiftState.CALLING, attentionStartDate = 2_000L))

        assertEquals(2, notifications.rows.size)
        assertEquals(listOf(1_000L, 2_000L), notifications.rows.map { it.attentionStartDate })
    }

    private fun service(
        shifts: FakeShiftRepository,
        notifications: InMemoryNotificationRepository,
        now: Long,
    ) = ShiftServiceImpl(
        shiftRepository = shifts,
        clientRepository = FakeClientRepository(),
        companySiteRepository = FakeCompanySiteRepository(
            mapOf("site-1" to CompanySite(id = "site-1", name = "Cafe")),
        ),
        turnReadyNotifier = TurnReadyNotifier(notifications),
        clock = { now },
    )

    private fun waitingShift() = Shift(
        id = "s1",
        date = 1L,
        parentCompanySite = "site-1",
        number = 4,
        contactId = "+57 300 111 2233",
        notes = null,
        state = ShiftState.WAITING,
    )
}
