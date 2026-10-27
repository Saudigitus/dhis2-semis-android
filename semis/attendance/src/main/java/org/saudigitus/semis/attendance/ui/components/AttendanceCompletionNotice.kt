package org.saudigitus.semis.attendance.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.saudigitus.semis.attendance.R
import org.saudigitus.semis.core.designsystem.components.notice.InlineNotice
import org.saudigitus.semis.core.designsystem.theme.dark_warning
import org.saudigitus.semis.core.designsystem.theme.light_success
import org.saudigitus.semis.core.designsystem.theme.contentTone

/**
 * States whether the attendance status event of the day has been completed, so the user
 * can tell a finished day from one that is still open.
 */
@Composable
internal fun AttendanceCompletionNotice(
    completed: Boolean,
    modifier: Modifier = Modifier,
) {
    InlineNotice(
        modifier = modifier,
        text = stringResource(
            if (completed) R.string.attendance_completed else R.string.attendance_incomplete,
        ),
        imageVector = if (completed) {
            Icons.Default.CheckCircle
        } else {
            Icons.Default.PendingActions
        },
        tone = if (completed) light_success.contentTone() else dark_warning,
    )
}
