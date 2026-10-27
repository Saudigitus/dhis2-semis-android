package org.saudigitus.campaign.core.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class BooleanFormattingTest {

    @Test
    fun `formats affirmative boolean values as true`() {
        assertEquals("true", "true".formatBoolean())
        assertEquals("true", "TRUE".formatBoolean())
        assertEquals("true", "1".formatBoolean())
    }

    @Test
    fun `formats negative boolean values as false`() {
        assertEquals("false", "false".formatBoolean())
        assertEquals("false", "FALSE".formatBoolean())
        assertEquals("false", "0".formatBoolean())
    }

    @Test
    fun `invalid or empty values remain unanswered`() {
        assertEquals("", "".formatBoolean())
        assertEquals("", "yes".formatBoolean())
    }
}
