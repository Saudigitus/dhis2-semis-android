package org.saudigitus.semis.attendance.ui.model

import org.junit.Assert.assertEquals
import org.junit.Test
import org.saudigitus.semis.core.data.model.app_config.StatusOption

class AttendanceStatusSummaryValuesTest {

    private fun statusOption(
        code: String?,
        key: String?,
        totalSummary: String?,
    ) = StatusOption(
        code = code,
        color = null,
        configKey = null,
        icon = null,
        key = key,
        totalSummary = totalSummary,
    )

    private val present = statusOption("PRESENT", "present", "de_present")
    private val absent = statusOption("ABSENT", "absent", "de_absent")
    private val late = statusOption("LATE", "late", "de_late")

    private fun counts(
        totalLearners: Int,
        attendanceValues: List<String>,
        codes: List<String> = listOf("ABSENT", "LATE"),
    ) = attendanceSummaryCounts(
        totalLearners = totalLearners,
        configuredStatusCodes = codes,
        attendanceValues = attendanceValues,
    )

    @Test
    fun `each configured status is written to the data element it declares`() {
        val values = attendanceStatusSummaryValues(
            statusOptions = listOf(present, absent, late),
            totalRecordsDataElement = "de_total",
            counts = counts(10, listOf("ABSENT", "ABSENT", "LATE")),
        ).toMap()

        assertEquals("10", values["de_total"])
        assertEquals("7", values["de_present"])
        assertEquals("2", values["de_absent"])
        assertEquals("1", values["de_late"])
    }

    @Test
    fun `a status configured without a summary data element is skipped`() {
        val values = attendanceStatusSummaryValues(
            statusOptions = listOf(present, absent, statusOption("LATE", "late", null)),
            totalRecordsDataElement = "de_total",
            counts = counts(5, listOf("ABSENT", "LATE")),
        ).toMap()

        assertEquals(setOf("de_total", "de_present", "de_absent"), values.keys)
        assertEquals("3", values["de_present"])
    }

    @Test
    fun `statuses added by configuration are collected without touching the mapping`() {
        val excused = statusOption("EXCUSED", "excused", "de_excused")

        val values = attendanceStatusSummaryValues(
            statusOptions = listOf(present, absent, late, excused),
            totalRecordsDataElement = "de_total",
            counts = counts(
                totalLearners = 8,
                attendanceValues = listOf("ABSENT", "EXCUSED", "EXCUSED"),
                codes = listOf("ABSENT", "LATE", "EXCUSED"),
            ),
        ).toMap()

        assertEquals("2", values["de_excused"])
        assertEquals("1", values["de_absent"])
        assertEquals("0", values["de_late"])
        assertEquals("5", values["de_present"])
    }

    @Test
    fun `the present status is counted as the learners the other statuses leave over`() {
        val values = attendanceStatusSummaryValues(
            statusOptions = listOf(present, absent),
            totalRecordsDataElement = "de_total",
            counts = counts(4, emptyList()),
        ).toMap()

        assertEquals("4", values["de_present"])
        assertEquals("0", values["de_absent"])
    }
}
