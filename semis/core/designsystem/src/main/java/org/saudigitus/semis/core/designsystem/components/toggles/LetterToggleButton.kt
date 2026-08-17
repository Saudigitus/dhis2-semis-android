package org.saudigitus.semis.core.designsystem.components.toggles

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.saudigitus.semis.core.designsystem.R

/**
 * Circular single-letter toggle. Tinted while unselected and solid once selected.
 */
@Composable
fun LetterToggleButton(
    option: LetterToggleOption,
    selected: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 34.dp,
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
        Text(
            text = option.letter,
            color = if (selected) option.selectedContentColor else option.contentColor,
            fontSize = 13.sp,
            maxLines = 1,
            fontFamily = FontFamily(Font(R.font.rubik_medium)),
        )
    }
}
