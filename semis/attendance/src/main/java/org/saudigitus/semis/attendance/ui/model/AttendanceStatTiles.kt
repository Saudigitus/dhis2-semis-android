package org.saudigitus.semis.attendance.ui.model

import org.saudigitus.semis.core.designsystem.components.bottomsheet.model.BottomSheetModel
import org.saudigitus.semis.core.designsystem.components.stats.StatTileModel
import org.saudigitus.semis.core.designsystem.theme.SemisPalette
import org.saudigitus.semis.core.designsystem.theme.readableContentColor

/**
 * Builds the header summary tiles: the learner total followed by one tile per
 * configured attendance status, in configuration order.
 *
 * Each status tile is painted with the color that status is configured with, so the
 * counters read the same way as the icons on the roster. The foreground follows the
 * lightness of that color, keeping the count legible whichever color is configured.
 *
 * The code-less default status is derived from the learners left over, so it would read
 * as everyone being present on a day nobody was recorded. Every status therefore reports
 * zero while [hasAttendanceRecord] is false, and the tiles start counting once the day
 * carries an attendance record.
 */
internal fun attendanceStatTiles(
    totalLabel: String,
    totalLearners: Int,
    summaries: List<BottomSheetModel>,
    hasAttendanceRecord: Boolean,
): List<StatTileModel> {
    val totalTile = StatTileModel(
        id = "total",
        label = totalLabel,
        value = totalLearners,
        containerColor = SemisPalette.HeaderBlueAccent,
    )

    val statusTiles = summaries.mapIndexed { index, summary ->
        val statusColor = summary.color
            ?: SemisPalette.TileTones[index % SemisPalette.TileTones.size]

        StatTileModel(
            id = summary.label ?: "status-$index",
            label = summary.label.orEmpty(),
            value = if (hasAttendanceRecord) summary.value?.toIntOrNull() ?: 0 else 0,
            containerColor = statusColor,
            contentColor = statusColor.readableContentColor(),
        )
    }

    return listOf(totalTile) + statusTiles
}
