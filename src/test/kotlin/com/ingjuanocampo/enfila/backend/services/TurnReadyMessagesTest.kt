package com.ingjuanocampo.enfila.backend.services

import com.ingjuanocampo.enfila.backend.data.models.Shift
import com.ingjuanocampo.enfila.backend.data.models.ShiftState
import com.ingjuanocampo.enfila.backend.data.models.UpdateShiftRequest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TurnReadyMessagesTest {
    @Test
    fun enteringCallingBuildsTheMessageAndNormalizesThePhone() {
        val prepared = prepareShiftUpdate(
            previous = shift(state = ShiftState.WAITING, contactId = "+57 300-111 2233"),
            request = UpdateShiftRequest(state = ShiftState.CALLING),
            siteName = " Cafe ",
            now = 1_000L,
        )

        assertEquals(1_000L, prepared.request.attentionStartDate)
        val draft = prepared.draft!!
        assertEquals("573001112233", draft.contactDigits)
        assertEquals("Es tu turno #4 en Cafe. Acércate por favor.", draft.body)
        assertEquals(1_000L, draft.attentionStartDate)
        assertTrue(draft.contentVariables.contains("\"1\":\"4\""))
        assertTrue(draft.contentVariables.contains("\"2\":\"Cafe\""))
    }

    @Test
    fun repeatingCallingDoesNotNotify() {
        val request = UpdateShiftRequest(state = ShiftState.CALLING, notes = "window")
        val prepared = prepareShiftUpdate(
            previous = shift(state = ShiftState.CALLING),
            request = request,
            siteName = "Cafe",
            now = 1_000L,
        )

        assertNull(prepared.draft)
        assertEquals(request, prepared.request)
    }

    @Test
    fun callingAgainAfterTheTurnLeftCallingNotifiesWithTheGivenTimestamp() {
        val prepared = prepareShiftUpdate(
            previous = shift(state = ShiftState.FINISHED),
            request = UpdateShiftRequest(state = ShiftState.CALLING, attentionStartDate = 50L),
            siteName = "  ",
            now = 1_000L,
        )

        assertEquals(50L, prepared.draft?.attentionStartDate)
        assertEquals("Es tu turno #4 en el local. Acércate por favor.", prepared.draft?.body)
    }

    @Test
    fun noteUpdateDoesNotNotify() {
        val prepared = prepareShiftUpdate(
            previous = shift(state = ShiftState.WAITING),
            request = UpdateShiftRequest(notes = "allergy"),
            siteName = "Cafe",
            now = 1_000L,
        )

        assertNull(prepared.draft)
    }

    @Test
    fun emptyPhoneSkipsTheDraftButStillStampsAttentionStart() {
        val prepared = prepareShiftUpdate(
            previous = shift(state = ShiftState.WAITING, contactId = "++"),
            request = UpdateShiftRequest(state = ShiftState.CALLING),
            siteName = "Cafe",
            now = 9L,
        )

        assertNull(prepared.draft)
        assertEquals(9L, prepared.request.attentionStartDate)
    }

    @Test
    fun normalizePhoneDigitsStripsFormatting() {
        assertEquals("573137550993", normalizePhoneDigits("whatsapp:+57 313-755-0993"))
        assertEquals("", normalizePhoneDigits("abc"))
    }

    private fun shift(state: ShiftState, contactId: String = "573001112233", number: Int = 4) = Shift(
        id = "s1",
        date = 1L,
        parentCompanySite = "site-1",
        number = number,
        contactId = contactId,
        notes = null,
        state = state,
    )
}
