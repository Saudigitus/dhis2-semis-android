package org.dhis2.data.server

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ClientVersionTest {

    @Test
    fun `the SEMIS version carries the Capture App version as build metadata`() {
        assertEquals("2.1.0-rc4+capture.3.4.2", clientVersion("2.1.0-rc4", "3.4.2"))
        assertEquals("2.1.0+capture.3.4.2", clientVersion("2.1.0", "3.4.2"))
    }

    @Test
    fun `the version never adds a field to the User-Agent`() {
        assertFalse(clientVersion("2.1.0", "3.4.2").contains("/"))
    }
}
