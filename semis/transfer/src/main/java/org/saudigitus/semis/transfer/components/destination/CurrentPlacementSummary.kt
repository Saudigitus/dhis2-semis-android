package org.saudigitus.semis.transfer.components.destination

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.saudigitus.semis.core.designsystem.components.FilterDetailsState
import org.saudigitus.semis.transfer.R

@Composable
internal fun CurrentPlacementSummary(state: FilterDetailsState) {
    val academicYearLabel = stringResource(R.string.academic_year)
    val schoolLabel = stringResource(R.string.school)
    val gradeLabel = stringResource(R.string.grade)
    val classLabel = stringResource(R.string.class_name)
    val details = listOfNotNull(
        state.academicYear.takeIf(String::isNotBlank)?.let { academicYearLabel to it },
        state.orgUnit.takeIf(String::isNotBlank)?.let { schoolLabel to it },
        state.grade?.takeIf(String::isNotBlank)?.let { gradeLabel to it },
        state.section?.takeIf(String::isNotBlank)?.let { classLabel to it },
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        details.forEachIndexed { index, (label, value) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = label,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = value,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            if (index < details.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 18.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
                )
            }
        }
    }
}
