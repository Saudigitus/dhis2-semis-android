package org.saudigitus.semis.attendance.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import org.saudigitus.semis.attendance.R
import org.saudigitus.semis.core.designsystem.components.text.SectionCaption

/**
 * Caption above the roster stating how many learners still miss an attendance status.
 */
@Composable
internal fun AttendanceListCaption(
    pendingCount: Int,
    modifier: Modifier = Modifier,
) {
    SectionCaption(
        text = if (pendingCount > 0) {
            pluralStringResource(
                R.plurals.attendance_pending_records,
                pendingCount,
                pendingCount,
            )
        } else {
            stringResource(R.string.attendance_all_recorded)
        },
        modifier = modifier,
    )
}
