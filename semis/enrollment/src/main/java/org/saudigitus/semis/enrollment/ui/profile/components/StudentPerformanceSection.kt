package org.saudigitus.semis.enrollment.ui.profile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.School
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.saudigitus.semis.core.data.model.profile.SubjectPerformance
import org.saudigitus.semis.core.designsystem.components.cards.DetailCard
import org.saudigitus.semis.core.designsystem.components.rows.LabelValueRow
import org.saudigitus.semis.core.designsystem.theme.SemisAccent
import org.saudigitus.semis.core.utils.DateHelper
import org.saudigitus.semis.enrollment.R

/**
 * Marks the learner collected per subject over the selected academic year, each subject
 * headed by the average those marks add up to.
 */
@Composable
internal fun StudentPerformanceSection(
    subjects: List<SubjectPerformance>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (subjects.isEmpty()) {
            DetailCard(
                title = stringResource(R.string.profile_performance),
                imageVector = Icons.Outlined.School,
                accent = SemisAccent.Amber,
            ) {
                ProfileEmptyLine(text = stringResource(R.string.profile_no_performance))
            }
            return@Column
        }

        subjects.forEach { subject ->
            DetailCard(
                title = subject.subject,
                subtitle = subject.average?.let { average ->
                    stringResource(R.string.profile_average, formatAverage(average))
                },
                imageVector = Icons.Outlined.School,
                accent = SemisAccent.Amber,
            ) {
                subject.marks.forEach { mark ->
                    LabelValueRow(
                        label = listOfNotNull(
                            mark.label.takeIf { it.isNotBlank() },
                            mark.date?.let { DateHelper.formatDate(it.time) },
                        ).joinToString(separator = " · "),
                        value = mark.displayValue,
                    )
                }
            }
        }
    }
}

private fun formatAverage(average: Double): String =
    if (average % 1.0 == 0.0) {
        average.toInt().toString()
    } else {
        String.format("%.1f", average)
    }
