package org.saudigitus.semis.transfer.components.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.saudigitus.semis.core.designsystem.components.chips.OutlinedActionChip
import org.saudigitus.semis.core.designsystem.theme.surfaceTone
import org.saudigitus.semis.transfer.model.TransferStatusFilter

/**
 * One chip per status. The chip in effect is filled in its own colour, and selecting it
 * again clears the filter, so the list can always be seen whole without a fourth chip.
 */
@Composable
internal fun TransferStatusChips(
    selected: TransferStatusFilter?,
    modifier: Modifier = Modifier,
    onSelect: (TransferStatusFilter) -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TransferStatusFilter.entries.forEach { filter ->
            val accent = filter.status.accentColor
            val active = filter == selected

            OutlinedActionChip(
                label = stringResource(filter.status.label),
                contentColor = if (active) Color.White else accent,
                borderColor = accent.surfaceTone(alpha = if (active) 1f else .35f),
                containerColor = if (active) accent else accent.surfaceTone(alpha = .06f),
                onClick = { onSelect(filter) },
            )
        }
    }
}
