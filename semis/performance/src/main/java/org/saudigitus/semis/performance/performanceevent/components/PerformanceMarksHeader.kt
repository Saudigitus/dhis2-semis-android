package org.saudigitus.semis.performance.performanceevent.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.saudigitus.semis.core.designsystem.theme.SemisPalette
import org.saudigitus.semis.performance.R

private val MissingAccent = Color(0xFFB45309)
private val MissingContainer = Color(0xFFFEF6E0)

/**
 * Title of the data element being captured, the class context and the tally of the marks
 * entered so far.
 */
@Composable
internal fun PerformanceMarksHeader(
    title: String,
    context: String,
    stats: MarksStats,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = SemisPalette.CardSurface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = SemisPalette.TextPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = stringResource(R.string.performance_students_count, stats.total),
                        style = MaterialTheme.typography.labelLarge,
                        color = SemisPalette.TextSecondary,
                        fontWeight = FontWeight.Medium,
                    )
                }
                if (context.isNotBlank()) {
                    Text(
                        text = context,
                        style = MaterialTheme.typography.bodySmall,
                        color = SemisPalette.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MarkTile(
                    value = "${stats.graded}",
                    label = stringResource(R.string.performance_stat_graded),
                    valueColor = SemisPalette.ActionBlue,
                )
                MarkTile(
                    value = "${stats.missing}",
                    label = stringResource(R.string.performance_stat_missing),
                    valueColor = MissingAccent,
                )
                MarkTile(
                    value = stats.average?.formatMark() ?: "–",
                    label = stringResource(R.string.performance_stat_average),
                    valueColor = SemisPalette.TextSecondary,
                )
                MarkTile(
                    value = stats.highest?.formatMark() ?: "–",
                    label = stringResource(R.string.performance_stat_highest),
                    valueColor = SemisPalette.TextSecondary,
                )
            }
        }
    }
}

@Composable
private fun RowScope.MarkTile(
    value: String,
    label: String,
    valueColor: Color,
) {
    Surface(
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(12.dp),
        color = SemisPalette.ScreenBackground,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = valueColor,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = SemisPalette.TextMuted,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** Reminder rendered above the roster while marks are still missing. */
@Composable
internal fun MissingMarksBanner(
    missing: Int,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MissingContainer,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.ErrorOutline,
                contentDescription = null,
                tint = MissingAccent,
            )
            Text(
                text = pluralStringResource(
                    R.plurals.performance_students_without_marks,
                    missing,
                    missing,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MissingAccent,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}
