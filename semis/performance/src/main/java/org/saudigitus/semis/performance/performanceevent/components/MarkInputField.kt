package org.saudigitus.semis.performance.performanceevent.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.saudigitus.semis.core.designsystem.theme.SemisAccent
import org.saudigitus.semis.core.designsystem.theme.SemisPalette
import org.saudigitus.semis.core.designsystem.theme.semisSoftShadow

private val FieldShape = RoundedCornerShape(12.dp)
private val NumericInput = "[-0-9.,]*".toRegex()

/**
 * Compact box where the mark of a single learner is typed, rendered at the end of the roster row.
 *
 * While the list is read only the box stays filled with the page tint, and it turns white with a
 * marked outline once the form is opened for edition, so tapping the action visibly unlocks the
 * whole roster.
 */
@Composable
internal fun MarkInputField(
    value: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()

    val container by animateColorAsState(
        targetValue = if (enabled) SemisPalette.CardSurface else SemisPalette.ScreenBackground,
        animationSpec = tween(durationMillis = 180),
        label = "mark_field_container",
    )
    val outline by animateColorAsState(
        targetValue = when {
            focused -> SemisAccent.Blue
            enabled -> SemisAccent.Blue.copy(alpha = 0.45f)
            else -> SemisPalette.CardBorder
        },
        animationSpec = tween(durationMillis = 180),
        label = "mark_field_outline",
    )
    val outlineWidth by animateDpAsState(
        targetValue = if (focused) 1.5.dp else 1.dp,
        animationSpec = tween(durationMillis = 180),
        label = "mark_field_outline_width",
    )

    BasicTextField(
        modifier = modifier
            .width(72.dp)
            .height(44.dp),
        value = value,
        onValueChange = { typed -> if (typed.matches(NumericInput)) onValueChange(typed) },
        enabled = enabled,
        singleLine = true,
        interactionSource = interactionSource,
        cursorBrush = SolidColor(SemisAccent.Blue),
        textStyle = TextStyle(
            color = if (enabled) SemisPalette.TextPrimary else SemisPalette.TextSecondary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done,
        ),
        decorationBox = { field ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .semisSoftShadow(FieldShape, elevation = if (enabled) 2.dp else 0.dp)
                    .background(color = container, shape = FieldShape)
                    .border(width = outlineWidth, color = outline, shape = FieldShape),
                contentAlignment = Alignment.Center,
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = "–",
                        style = MaterialTheme.typography.titleMedium,
                        color = SemisPalette.TextMuted,
                    )
                }
                field()
            }
        },
    )
}
