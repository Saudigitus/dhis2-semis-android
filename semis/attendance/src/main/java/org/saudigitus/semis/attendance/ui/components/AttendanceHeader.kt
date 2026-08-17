package org.saudigitus.semis.attendance.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.saudigitus.semis.attendance.R
import org.saudigitus.semis.core.designsystem.components.CustomDatePicker
import org.saudigitus.semis.core.designsystem.components.buttons.CircularIconButton
import org.saudigitus.semis.core.designsystem.components.header.HeaderTitleBar
import org.saudigitus.semis.core.designsystem.components.header.RoundedBottomHeader
import org.saudigitus.semis.core.designsystem.components.model.ToolbarHeaders
import org.saudigitus.semis.core.designsystem.components.pills.StatusPill
import org.saudigitus.semis.core.designsystem.components.stats.StatTileModel
import org.saudigitus.semis.core.designsystem.components.stats.StatTileRow

/**
 * Attendance screen header: navigation, the selected date, the synchronization state and
 * the live status counters. Tapping the date opens the school-calendar date picker.
 */
@Composable
internal fun AttendanceHeader(
    headers: ToolbarHeaders,
    tiles: List<StatTileModel>,
    pendingSyncCount: Int,
    modifier: Modifier = Modifier,
    dateValidator: (Long) -> Boolean = { true },
    onNavigateBack: () -> Unit,
    onSync: () -> Unit,
    onDateSelected: (String) -> Unit,
) {
    var isCalendarShown by remember { mutableStateOf(false) }

    CustomDatePicker(
        show = isCalendarShown,
        dismiss = { isCalendarShown = false },
        onDatePick = onDateSelected,
        dateValidator = dateValidator,
    )

    RoundedBottomHeader(modifier = modifier) {
        HeaderTitleBar(
            title = headers.title,
            subtitle = headers.subtitle,
            onNavigateBack = onNavigateBack,
            onSubtitleClick = { isCalendarShown = true },
            trailing = {
                StatusPill(
                    text = if (pendingSyncCount > 0) {
                        stringResource(R.string.attendance_unsynced_count, pendingSyncCount)
                    } else {
                        stringResource(R.string.attendance_synced)
                    },
                    imageVector = if (pendingSyncCount > 0) {
                        Icons.Default.SyncProblem
                    } else {
                        Icons.Default.Sync
                    },
                    onClick = onSync,
                )

                CircularIconButton(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = stringResource(R.string.select_attendance_date),
                    onClick = { isCalendarShown = true },
                )
            },
        )

        StatTileRow(tiles = tiles)
    }
}
