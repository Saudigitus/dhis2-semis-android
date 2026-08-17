package org.saudigitus.semis.attendance.ui.model

import org.saudigitus.semis.core.designsystem.components.bottomsheet.model.BottomSheetModel
import org.saudigitus.semis.core.designsystem.components.stats.StatTileModel
import org.saudigitus.semis.core.designsystem.theme.SemisPalette

/**
 * Builds the header summary tiles: the learner total followed by one tile per
 * configured attendance status, in configuration order.
 *
 * The code-less default status is derived from the learners left over, so it would read
 * as everyone being present on a day nobody was recorded. While [hasAttendanceRecord] is
 * false every status therefore reports zero, and the screen states instead that the day
 * has no attendance yet.
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
        StatTileModel(
            id = summary.label ?: "status-$index",
            label = summary.label.orEmpty(),
            value = if (hasAttendanceRecord) summary.value?.toIntOrNull() ?: 0 else 0,
            containerColor = SemisPalette.TileTones[index % SemisPalette.TileTones.size],
        )
    }

    return listOf(totalTile) + statusTiles
}
