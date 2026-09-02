package org.saudigitus.semis.enrollment.ui.profile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.saudigitus.semis.core.data.model.profile.AttendanceHistory
import org.saudigitus.semis.core.designsystem.components.cards.DetailCard
import org.saudigitus.semis.core.designsystem.components.rows.LabelValueRow
import org.saudigitus.semis.core.designsystem.components.text.SectionCaption
import org.saudigitus.semis.core.designsystem.theme.SemisAccent
import org.saudigitus.semis.core.utils.DateHelper
import org.saudigitus.semis.enrollment.R

/**
 * Attendance the learner accumulated over the selected academic year: a count per
 * configured status, then the days themselves, most recent first.
 */
@Composable
internal fun StudentAttendanceSection(
    history: AttendanceHistory,
    modifier: Modifier = Modifier,
    maxRecords: Int = 30,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        DetailCard(
            title = stringResource(R.string.profile_attendance_summary),
            subtitle = stringResource(R.string.profile_recorded_days, history.recordedDays),
            imageVector = Icons.Outlined.EventAvailable,
            accent = SemisAccent.Teal,
        ) {
            if (history.statusCounts.isEmpty()) {
                ProfileEmptyLine(text = stringResource(R.string.profile_no_attendance))
            } else {
                history.statusCounts.forEach { status ->
                    LabelValueRow(label = status.label, value = "${status.count}")
                }
            }
        }

        if (history.records.isNotEmpty()) {
            DetailCard(
                title = stringResource(R.string.profile_attendance_history),
                imageVector = Icons.Outlined.EventAvailable,
                accent = SemisAccent.Blue,
            ) {
                history.records.take(maxRecords).forEach { record ->
                    LabelValueRow(
                        label = record.date?.let { DateHelper.formatDate(it.time) }
                            ?: stringResource(R.string.profile_unknown_date),
                        value = listOfNotNull(record.statusLabel, record.absenceReason)
                            .joinToString(separator = " · "),
                    )
                }

                if (history.records.size > maxRecords) {
                    SectionCaption(
                        text = stringResource(
                            R.string.profile_more_records,
                            history.records.size - maxRecords,
                        ),
                    )
                }
            }
        }
    }
}
