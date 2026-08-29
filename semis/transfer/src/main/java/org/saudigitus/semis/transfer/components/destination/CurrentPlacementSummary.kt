package org.saudigitus.semis.transfer.components.destination

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.saudigitus.semis.core.designsystem.components.FilterDetailsState
import org.saudigitus.semis.core.designsystem.components.cards.DetailRows
import org.saudigitus.semis.transfer.R

/** Where the learner currently sits, read back with the shared detail rows. */
@Composable
internal fun CurrentPlacementSummary(state: FilterDetailsState) {
    val academicYearLabel = stringResource(R.string.academic_year)
    val schoolLabel = stringResource(R.string.school)
    val gradeLabel = stringResource(R.string.grade)
    val classLabel = stringResource(R.string.class_name)

    DetailRows(
        rows = listOfNotNull(
            state.academicYear.takeIf(String::isNotBlank)?.let { academicYearLabel to it },
            state.orgUnit.takeIf(String::isNotBlank)?.let { schoolLabel to it },
            state.grade?.takeIf(String::isNotBlank)?.let { gradeLabel to it },
            state.section?.takeIf(String::isNotBlank)?.let { classLabel to it },
        ),
    )
}
