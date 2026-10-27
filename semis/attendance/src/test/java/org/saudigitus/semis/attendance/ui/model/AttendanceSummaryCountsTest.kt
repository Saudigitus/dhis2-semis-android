package org.saudigitus.semis.attendance.ui.model

import org.junit.Assert.assertEquals
import org.junit.Test

class AttendanceSummaryCountsTest {

    @Test
    fun `present learners exclude all configured non-present statuses`() {
        val summary = attendanceSummaryCounts(
            totalLearners = 10,
            configuredStatusCodes = listOf("ABSENT", "LATE", "EXCUSED"),
            attendanceValues = listOf("ABSENT", "ABSENT", "LATE"),
        )

        assertEquals(10, summary.totalLearners)
        assertEquals(3, summary.totalAbsences)
        assertEquals(2, summary.statusCounts["ABSENT"])
        assertEquals(1, summary.statusCounts["LATE"])
        assertEquals(0, summary.statusCounts["EXCUSED"])
        assertEquals(7, summary.presentLearners)
    }

    @Test
    fun `unknown attendance values do not reduce present learners`() {
        val summary = attendanceSummaryCounts(
            totalLearners = 4,
            configuredStatusCodes = listOf("ABSENT"),
            attendanceValues = listOf("ABSENT", "UNKNOWN"),
        )

        assertEquals(3, summary.presentLearners)
    }

    @Test
    fun `all learners are present when there are no non-present events`() {
        val summary = attendanceSummaryCounts(
            totalLearners = 5,
            configuredStatusCodes = listOf("ABSENT", "LATE"),
            attendanceValues = emptyList(),
        )

        assertEquals(5, summary.presentLearners)
    }
}
