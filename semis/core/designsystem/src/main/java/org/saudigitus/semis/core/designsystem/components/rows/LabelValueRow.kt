package org.saudigitus.semis.core.designsystem.components.rows

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.saudigitus.semis.core.designsystem.R
import org.saudigitus.semis.core.designsystem.theme.SemisPalette

/**
 * A labelled value: the label on the left, the value aligned to the right.
 *
 * A missing value is stated rather than left blank, so a reader can tell an empty field
 * apart from one that was never captured.
 */
@Composable
fun LabelValueRow(
    label: String,
    value: String?,
    modifier: Modifier = Modifier,
    emptyValueLabel: String = stringResource(R.string.not_recorded),
) {
    val recorded = value?.takeIf { it.isNotBlank() }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            color = SemisPalette.TextSecondary,
            fontSize = 12.5.sp,
            lineHeight = 17.sp,
            fontFamily = FontFamily(Font(R.font.rubik_regular)),
        )
        Text(
            text = recorded ?: emptyValueLabel,
            modifier = Modifier.weight(1f),
            color = if (recorded != null) {
                SemisPalette.TextPrimary
            } else {
                SemisPalette.TextMuted
            },
            fontSize = 12.5.sp,
            lineHeight = 17.sp,
            textAlign = TextAlign.End,
            fontStyle = if (recorded == null) FontStyle.Italic else FontStyle.Normal,
            fontFamily = FontFamily(
                Font(if (recorded != null) R.font.rubik_medium else R.font.rubik_regular),
            ),
        )
    }
}
