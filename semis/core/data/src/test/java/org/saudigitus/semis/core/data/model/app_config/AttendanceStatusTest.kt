package org.saudigitus.semis.core.data.model.app_config

import org.junit.Assert.assertEquals
import org.junit.Test
import org.saudigitus.semis.core.utils.JsonMapper

class AttendanceStatusTest {

    @Test
    fun `reads total data elements from attendance status property names`() {
        val status = JsonMapper.json.decodeFromString<AttendanceStatus>(
            """{"totalAbsences":"absences","totalRecords":"records"}"""
        )

        assertEquals("absences", status.totalAbsences)
        assertEquals("records", status.totalRecords)
    }

    @Test
    fun `reads legacy total data element property names`() {
        val status = JsonMapper.json.decodeFromString<AttendanceStatus>(
            """{
                "totalAbsencesDataElement":"absences",
                "totalRecordsDataElement":"records"
            }""".trimIndent()
        )

        assertEquals("absences", status.totalAbsences)
        assertEquals("records", status.totalRecords)
    }

    @Test
    fun `uses attendance status flow only when enabled configuration is complete`() {
        val complete = AttendanceStatus(
            allowAttendanceStatus = true,
            program = "program",
            programStage = "stage",
            totalAbsences = "absences",
            totalRecords = "records",
        )

        assertEquals(true, complete.isEnabledAndConfigured())
        assertEquals(false, complete.copy(totalRecords = null).isEnabledAndConfigured())
        assertEquals(false, complete.copy(allowAttendanceStatus = false).isEnabledAndConfigured())
        assertEquals(false, null.isEnabledAndConfigured())
    }
}
