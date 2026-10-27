package org.saudigitus.semis.attendance.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.saudigitus.semis.attendance.R
import org.saudigitus.semis.core.designsystem.attendance.model.AttendanceButtonModel
import org.saudigitus.semis.core.designsystem.components.chips.LabeledChipRow
import org.saudigitus.semis.core.designsystem.components.chips.OutlinedActionChip
import org.saudigitus.semis.core.designsystem.theme.SemisPalette
import org.saudigitus.semis.core.designsystem.theme.borderTone
import org.saudigitus.semis.core.designsystem.theme.contentTone
import org.saudigitus.semis.core.designsystem.theme.surfaceTone
import org.saudigitus.semis.core.designsystem.utils.UiDefaults

/**
 * Applies one configured attendance status to every learner of the current list.
 */
@Composable
internal fun AttendanceBulkBar(
    buttons: List<AttendanceButtonModel>,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onBulk: (AttendanceButtonModel) -> Unit,
) {
    if (buttons.isEmpty()) return

    LabeledChipRow(
        label = stringResource(R.string.bulk_label),
        modifier = modifier
            .fillMaxWidth()
            .background(color = SemisPalette.CardSurface)
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        buttons.forEach { button ->
            val statusColor = button.color
                ?: UiDefaults.getAttendanceStatusColor(button.key)
            val statusLabel = button.name?.takeIf { it.isNotBlank() } ?: button.key

            OutlinedActionChip(
                label = stringResource(R.string.bulk_all_status, statusLabel),
                contentColor = statusColor.contentTone(),
                borderColor = statusColor.borderTone(),
                containerColor = statusColor.surfaceTone(alpha = .10f),
                enabled = enabled && button.enabled,
                onClick = { onBulk(button) },
            )
        }
    }
}
