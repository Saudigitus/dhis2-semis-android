package org.saudigitus.semis.attendance.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import org.saudigitus.semis.attendance.R
import org.saudigitus.semis.core.designsystem.components.CustomDatePicker
import org.saudigitus.semis.core.designsystem.components.FilterDetailsState
import org.saudigitus.semis.core.designsystem.components.buttons.TonalIconButton
import org.saudigitus.semis.core.designsystem.components.header.HeaderTitleBar
import org.saudigitus.semis.core.designsystem.components.header.RoundedBottomHeader
import org.saudigitus.semis.core.designsystem.components.model.ToolbarHeaders
import org.saudigitus.semis.core.designsystem.components.pills.StatusPill
import org.saudigitus.semis.core.designsystem.components.stats.StatTileModel
import org.saudigitus.semis.core.designsystem.components.stats.StatTileRow

/** Shared height of the header actions, so the sync pill and the calendar button align. */
private val HeaderActionHeight = 32.dp

/**
 * Attendance screen header: navigation, the selected date, the synchronization state, the
 * live status counters and the filter context the list is scoped to.
 */
@Composable
internal fun AttendanceHeader(
    headers: ToolbarHeaders,
    tiles: List<StatTileModel>,
    pendingSyncCount: Int,
    filterDetailsState: FilterDetailsState,
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

    RoundedBottomHeader(
        modifier = modifier,
        verticalSpacing = 12.dp,
    ) {
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
                    modifier = Modifier.height(HeaderActionHeight),
                    onClick = onSync,
                )

                TonalIconButton(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = stringResource(R.string.select_attendance_date),
                    size = HeaderActionHeight,
                    iconSize = 17.dp,
                    shape = RoundedCornerShape(10.dp),
                    onClick = { isCalendarShown = true },
                )
            },
        )

        StatTileRow(tiles = tiles)

        AttendanceFilterInfo(state = filterDetailsState)
    }
}
