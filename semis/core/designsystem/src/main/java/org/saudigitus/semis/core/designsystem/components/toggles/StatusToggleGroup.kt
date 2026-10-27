package org.saudigitus.semis.core.designsystem.components.toggles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Row of mutually exclusive [StatusToggleButton]s.
 *
 * While [expanded] is false only the selected status is drawn, so a recorded row reads as
 * a single badge; every choice is offered once the row becomes editable.
 */
@Composable
fun StatusToggleGroup(
    options: List<StatusToggleOption>,
    selectedId: String?,
    modifier: Modifier = Modifier,
    expanded: Boolean = true,
    spacing: Dp = 6.dp,
    buttonSize: Dp = 34.dp,
    onSelect: (StatusToggleOption) -> Unit,
) {
    val visibleOptions = if (expanded) {
        options
    } else {
        options.filter { it.id == selectedId }
    }

    if (visibleOptions.isEmpty()) return

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        visibleOptions.forEach { option ->
            StatusToggleButton(
                option = option,
                selected = option.id == selectedId,
                size = buttonSize,
                onClick = { onSelect(option) },
            )
        }
    }
}
