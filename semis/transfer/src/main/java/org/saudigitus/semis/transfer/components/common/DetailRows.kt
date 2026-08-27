package org.saudigitus.semis.transfer.components.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Label on the left, value on the right, one per line, divided.
 *
 * Every place the transfer module states a set of details uses this, so the learner
 * card, the current enrollment and the destination all line up with each other.
 *
 * [leadingDivider] separates the details from whatever sits above them, which the
 * learner card needs and a card of its own does not.
 */
@Composable
internal fun DetailRows(
    details: List<Pair<String, String>>,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 18.dp,
    verticalPadding: Dp = 10.dp,
    leadingDivider: Boolean = false,
) {
    if (details.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        if (leadingDivider) {
            DetailDivider(horizontalPadding = horizontalPadding)
        }

        details.forEachIndexed { index, (label, value) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = horizontalPadding,
                        vertical = verticalPadding,
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = label,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    modifier = Modifier
                        .padding(start = 18.dp)
                        .weight(1f, fill = false),
                    text = value,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.End,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (index < details.lastIndex) {
                DetailDivider(horizontalPadding = horizontalPadding)
            }
        }
    }
}

@Composable
private fun DetailDivider(horizontalPadding: Dp) {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = horizontalPadding),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
    )
}
