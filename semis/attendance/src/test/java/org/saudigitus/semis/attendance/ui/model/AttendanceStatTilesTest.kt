package org.saudigitus.semis.attendance.ui.model

import org.junit.Assert.assertEquals
import org.saudigitus.semis.core.designsystem.components.bottomsheet.model.BottomSheetModel
import org.junit.Test

class AttendanceStatTilesTest {

    private val summaries = listOf(
        BottomSheetModel(label = "Present", value = "7"),
        BottomSheetModel(label = "Absent", value = "2"),
        BottomSheetModel(label = "Late", value = "1"),
    )

    @Test
    fun `status counts are reported once the day has an attendance record`() {
        val tiles = attendanceStatTiles(
            totalLabel = "Total",
            totalLearners = 10,
            summaries = summaries,
            hasAttendanceRecord = true,
        )

        assertEquals(listOf("Total", "Present", "Absent", "Late"), tiles.map { it.label })
        assertEquals(listOf(10, 7, 2, 1), tiles.map { it.value })
    }

    @Test
    fun `every status reports zero while the day has no attendance record`() {
        val tiles = attendanceStatTiles(
            totalLabel = "Total",
            totalLearners = 10,
            summaries = summaries,
            hasAttendanceRecord = false,
        )

        assertEquals(10, tiles.first().value)
        assertEquals(listOf(0, 0, 0), tiles.drop(1).map { it.value })
    }

    @Test
    fun `the total tile is kept when no status is configured`() {
        val tiles = attendanceStatTiles(
            totalLabel = "Total",
            totalLearners = 3,
            summaries = emptyList(),
            hasAttendanceRecord = true,
        )

        assertEquals(1, tiles.size)
        assertEquals(3, tiles.first().value)
    }

    @Test
    fun `status tones cycle so any number of configured statuses stays distinct`() {
        val tiles = attendanceStatTiles(
            totalLabel = "Total",
            totalLearners = 6,
            summaries = summaries + BottomSheetModel(label = "Excused", value = "0"),
            hasAttendanceRecord = true,
        )

        val statusColors = tiles.drop(1).map { it.containerColor }

        assertEquals(statusColors.size, statusColors.distinct().size)
    }
}
