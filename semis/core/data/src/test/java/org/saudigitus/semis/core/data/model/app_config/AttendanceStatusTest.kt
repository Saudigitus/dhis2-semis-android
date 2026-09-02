package org.saudigitus.semis.core.data.model.app_config

import org.junit.Assert.assertEquals
import org.junit.Test
import org.saudigitus.semis.core.utils.JsonMapper

class AttendanceStatusTest {

    @Test
    fun `reads the total records data element`() {
        val status = JsonMapper.json.decodeFromString<AttendanceStatus>(
            """{"totalRecords":"records"}"""
        )

        assertEquals("records", status.totalRecords)
    }

    @Test
    fun `uses attendance status flow only when enabled configuration is complete`() {
        val complete = AttendanceStatus(
            allowAttendanceStatus = true,
            program = "program",
            programStage = "stage",
            totalRecords = "records",
        )

        assertEquals(true, complete.isEnabledAndConfigured())
        assertEquals(false, complete.copy(totalRecords = null).isEnabledAndConfigured())
        assertEquals(false, complete.copy(programStage = null).isEnabledAndConfigured())
        assertEquals(false, complete.copy(program = null).isEnabledAndConfigured())
        assertEquals(false, complete.copy(allowAttendanceStatus = false).isEnabledAndConfigured())
        assertEquals(false, null.isEnabledAndConfigured())
    }
}
