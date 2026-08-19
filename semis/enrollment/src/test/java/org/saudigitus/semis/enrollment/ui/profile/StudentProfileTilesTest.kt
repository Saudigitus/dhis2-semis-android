package org.saudigitus.semis.enrollment.ui.profile

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test
import org.saudigitus.semis.core.data.model.profile.AttendanceHistory
import org.saudigitus.semis.core.data.model.profile.AttendanceRecord
import org.saudigitus.semis.core.data.model.profile.AttendanceStatusCount
import org.saudigitus.semis.core.data.model.profile.TeiProfile

class StudentProfileTilesTest {

    private fun statusCount(
        key: String,
        count: Int,
        color: String? = null,
    ) = AttendanceStatusCount(
        key = key,
        code = key.uppercase(),
        label = key.replaceFirstChar { it.uppercase() },
        color = color,
        count = count,
    )

    private fun record(index: Int) = AttendanceRecord(
        eventUid = "event-$index",
        date = null,
        statusCode = "PRESENT",
        statusLabel = "Present",
        absenceReason = null,
    )

    private fun profile(
        statuses: List<AttendanceStatusCount>,
        recordedDays: Int = 0,
    ) = TeiProfile(
        teiUid = "tei",
        name = "Miranda Bailer",
        systemId = "2025-00000085",
        attendance = AttendanceHistory(
            records = (0 until recordedDays).map(::record),
            statusCounts = statuses,
        ),
    )

    @Test
    fun `the recorded days lead the counters and each configured status follows`() {
        val tiles = studentProfileTiles(
            recordedDaysLabel = "Recorded",
            profile = profile(
                statuses = listOf(
                    statusCount("present", 2),
                    statusCount("absent", 1),
                ),
                recordedDays = 3,
            ),
        )

        assertEquals(listOf("Recorded", "Present", "Absent"), tiles.map { it.label })
        assertEquals(listOf(3, 2, 1), tiles.map { it.value })
    }

    @Test
    fun `a status the learner has no day of still reports zero`() {
        val tiles = studentProfileTiles(
            recordedDaysLabel = "Recorded",
            profile = profile(
                statuses = listOf(
                    statusCount("present", 0),
                    statusCount("absent", 0),
                    statusCount("late", 0),
                ),
            ),
        )

        assertEquals(listOf("Recorded", "Present", "Absent", "Late"), tiles.map { it.label })
        assertEquals(listOf(0, 0, 0, 0), tiles.map { it.value })
    }

    @Test
    fun `no counter is built when the program configures no attendance status`() {
        val tiles = studentProfileTiles(
            recordedDaysLabel = "Recorded",
            profile = profile(statuses = emptyList(), recordedDays = 4),
        )

        assertEquals(emptyList<String>(), tiles.map { it.label })
    }

    @Test
    fun `no counter is built until the profile is loaded`() {
        assertEquals(
            emptyList<String>(),
            studentProfileTiles(recordedDaysLabel = "Recorded", profile = null).map { it.label },
        )
    }

    @Test
    fun `a status is painted with the color it is configured with`() {
        val tiles = studentProfileTiles(
            recordedDaysLabel = "Recorded",
            profile = profile(statuses = listOf(statusCount("absent", 1, color = "#E57373"))),
        )

        assertEquals(Color(0xFFE57373), tiles[1].containerColor)
    }

    @Test
    fun `a status without a configured color falls back to its key default`() {
        val tiles = studentProfileTiles(
            recordedDaysLabel = "Recorded",
            profile = profile(statuses = listOf(statusCount("late", 1))),
        )

        assertEquals(Color(0xFFFACC95), tiles[1].containerColor)
    }
}
