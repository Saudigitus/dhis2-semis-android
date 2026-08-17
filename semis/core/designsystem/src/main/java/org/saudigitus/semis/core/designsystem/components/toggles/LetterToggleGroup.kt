package org.saudigitus.semis.core.designsystem.components.toggles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Row of mutually exclusive [LetterToggleButton]s.
 */
@Composable
fun LetterToggleGroup(
    options: List<LetterToggleOption>,
    selectedId: String?,
    modifier: Modifier = Modifier,
    spacing: Dp = 6.dp,
    buttonSize: Dp = 34.dp,
    onSelect: (LetterToggleOption) -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        options.forEach { option ->
            LetterToggleButton(
                option = option,
                selected = option.id == selectedId,
                size = buttonSize,
                modifier = Modifier.semantics { contentDescription = option.label },
                onClick = { onSelect(option) },
            )
        }
    }
}
