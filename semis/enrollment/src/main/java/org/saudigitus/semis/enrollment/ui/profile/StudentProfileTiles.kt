package org.saudigitus.semis.enrollment.ui.profile

import org.saudigitus.semis.core.data.model.profile.TeiProfile
import org.saudigitus.semis.core.designsystem.components.stats.StatTileModel
import org.saudigitus.semis.core.designsystem.theme.SemisPalette
import org.saudigitus.semis.core.designsystem.theme.readableContentColor
import org.saudigitus.semis.core.designsystem.utils.UiDefaults

/**
 * Header counters of the dashboard: how many days were recorded, followed by one tile per
 * configured attendance status painted with the color that status is configured with.
 *
 * A status the learner holds no day of still reports zero, so the counters read the same
 * from one learner to the next. They are dropped altogether only when the program
 * configures no attendance status to count.
 */
internal fun studentProfileTiles(
    recordedDaysLabel: String,
    profile: TeiProfile?,
): List<StatTileModel> {
    val attendance = profile?.attendance ?: return emptyList()

    if (attendance.statusCounts.isEmpty()) return emptyList()

    val recordedTile = StatTileModel(
        id = "recorded",
        label = recordedDaysLabel,
        value = attendance.recordedDays,
        containerColor = SemisPalette.HeaderBlueAccent,
    )

    val statusTiles = attendance.statusCounts.map { status ->
        val statusColor = UiDefaults.getAttendanceStatusColor(
            status.key.ifBlank { status.code },
            status.color.orEmpty(),
        )

        StatTileModel(
            id = status.code,
            label = status.label,
            value = status.count,
            containerColor = statusColor,
            contentColor = statusColor.readableContentColor(),
        )
    }

    return listOf(recordedTile) + statusTiles
}
