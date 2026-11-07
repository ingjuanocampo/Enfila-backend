package com.ingjuanocampo.enfila.backend.services

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TurnReadyFormTest {
    @Test
    fun freeformBodyUsesTheSandboxSender() {
        val parameters = turnReadyFormParameters(
            fromDigits = "14155238886",
            toDigits = "573001112233",
            body = "Es tu turno #4 en Cafe. Acércate por favor.",
            contentSid = null,
            contentVariablesJson = null,
        )

        assertEquals("whatsapp:+573001112233", parameters["To"])
        assertEquals("whatsapp:+14155238886", parameters["From"])
        assertEquals("Es tu turno #4 en Cafe. Acércate por favor.", parameters["Body"])
        assertNull(parameters["ContentSid"])
    }

    @Test
    fun contentSidReplacesTheFreeformBody() {
        val parameters = turnReadyFormParameters(
            fromDigits = "14155238886",
            toDigits = "573001112233",
            body = "ignored",
            contentSid = "HXabc",
            contentVariablesJson = """{"1":"4","2":"Cafe"}""",
        )

        assertEquals("HXabc", parameters["ContentSid"])
        assertEquals("""{"1":"4","2":"Cafe"}""", parameters["ContentVariables"])
        assertNull(parameters["Body"])
    }
}
