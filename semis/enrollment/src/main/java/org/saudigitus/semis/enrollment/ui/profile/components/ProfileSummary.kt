package org.saudigitus.semis.enrollment.ui.profile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.saudigitus.semis.core.data.model.profile.ProfileFigure
import org.saudigitus.semis.core.data.model.profile.ProfileFigureKind
import org.saudigitus.semis.core.designsystem.theme.SemisPalette
import org.saudigitus.semis.enrollment.R

/**
 * The figures of the year at the top of the page, drawn like the counters of the performance
 * screen. They are counted from the records the page shows, so they agree with the tabs below.
 */
@Composable
internal fun ProfileSummary(
    figures: List<ProfileFigure>,
    modifier: Modifier = Modifier,
) {
    // Three tiles fit a phone; more go on the next row rather than being squeezed.
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        figures.chunked(TILES_PER_ROW).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { figure -> FigureTile(figure = figure) }
                repeat(TILES_PER_ROW - row.size) { Box(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun RowScope.FigureTile(figure: ProfileFigure) {
    Surface(
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(12.dp),
        color = SemisPalette.CardSurface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = figure.value,
                style = MaterialTheme.typography.titleMedium,
                color = SemisPalette.ActionBlue,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
            )
            Text(
                text = when (figure.kind) {
                    ProfileFigureKind.ABSENCES -> stringResource(R.string.profile_figure_absences)
                    ProfileFigureKind.AVERAGE ->
                        stringResource(R.string.profile_figure_average, figure.subject.orEmpty())
                },
                style = MaterialTheme.typography.labelSmall,
                color = SemisPalette.TextMuted,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private const val TILES_PER_ROW = 3
