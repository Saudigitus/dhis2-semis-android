package org.saudigitus.semis.attendance.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.saudigitus.semis.attendance.R
import org.saudigitus.semis.core.designsystem.components.FilterDetailsState
import org.saudigitus.semis.core.designsystem.components.stats.StatTileModel
import org.saudigitus.semis.core.designsystem.components.stats.StatTileRow
import org.saudigitus.semis.core.designsystem.theme.SemisPalette

/**
 * Says which class is on screen and how the day is going, on the sheet below the header.
 *
 * Laid out as the performance capture screen lays out its own summary, so that two screens
 * doing the same job, naming a class and tallying it, are read the same way. The school
 * takes the place performance gives to the subject, being what names the group here.
 *
 * The tiles keep the colour of the status they count, unlike performance where the numbers
 * are neutral: presence and absence are read at a glance and losing that would cost more
 * than the calm it buys.
 */
@Composable
internal fun AttendanceClassSummary(
    filterDetailsState: FilterDetailsState,
    tiles: List<StatTileModel>,
    totalLearners: Int,
    modifier: Modifier = Modifier,
) {
    // The header colour is painted behind the sheet so that the rounded top corners have
    // something to curve over. Without it the corners fall on the screen background and the
    // seam between the two reads as a straight cut. It has to be the colour the bar itself
    // uses, or the two blues meet and the join is visible as a band.
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(SemisPalette.HeaderBlueAccent),
    ) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = SummarySheetShape,
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
                        text = filterDetailsState.orgUnit,
                        style = MaterialTheme.typography.titleMedium,
                        color = SemisPalette.TextPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = pluralStringResource(
                            R.plurals.attendance_students_count,
                            totalLearners,
                            totalLearners,
                        ),
                        style = MaterialTheme.typography.labelLarge,
                        color = SemisPalette.TextSecondary,
                        fontWeight = FontWeight.Medium,
                    )
                }

                val context = listOfNotNull(
                    filterDetailsState.grade,
                    filterDetailsState.section,
                    filterDetailsState.academicYear,
                ).filter { it.isNotBlank() }.joinToString(separator = " · ")

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

            StatTileRow(tiles = tiles)
        }
    }
    }
}

/** Curves over the header, which is flat where the two meet. */
private val SummarySheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
