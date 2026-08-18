package org.saudigitus.semis.core.designsystem.components.toggles

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Circular status toggle. Tinted while unselected and solid once selected.
 */
@Composable
fun StatusToggleButton(
    option: StatusToggleOption,
    selected: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 34.dp,
    iconSize: Dp = 19.dp,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(
                color = if (selected) option.selectedContainerColor else option.containerColor,
                shape = CircleShape,
            )
            .border(
                width = 1.dp,
                color = if (selected) option.selectedContainerColor else option.borderColor,
                shape = CircleShape,
            )
            .clickable(
                enabled = option.enabled,
                role = Role.RadioButton,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = option.icon,
            contentDescription = option.label,
            tint = if (selected) option.selectedContentColor else option.contentColor,
            modifier = Modifier.size(iconSize),
        )
    }
}
