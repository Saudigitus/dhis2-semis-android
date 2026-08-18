package org.saudigitus.semis.performance.performanceevent.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.saudigitus.semis.core.designsystem.theme.SemisPalette

private val FieldShape = RoundedCornerShape(12.dp)
private val NumericInput = "[-0-9.,]*".toRegex()

/**
 * Compact box where the mark of a single learner is typed, rendered at the end of the roster row.
 */
@Composable
internal fun MarkInputField(
    value: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit,
) {
    BasicTextField(
        modifier = modifier
            .width(72.dp)
            .height(44.dp),
        value = value,
        onValueChange = { typed -> if (typed.matches(NumericInput)) onValueChange(typed) },
        enabled = enabled,
        singleLine = true,
        textStyle = TextStyle(
            color = SemisPalette.TextPrimary,
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
                    .background(color = SemisPalette.ScreenBackground, shape = FieldShape)
                    .border(width = 1.dp, color = SemisPalette.CardBorder, shape = FieldShape),
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
