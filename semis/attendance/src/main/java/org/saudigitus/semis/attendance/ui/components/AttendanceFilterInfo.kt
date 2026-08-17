package org.saudigitus.semis.attendance.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Stairs
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.saudigitus.semis.core.designsystem.R
import org.saudigitus.semis.core.designsystem.components.FilterDetailsState
import org.saudigitus.semis.core.designsystem.components.filters.FilterInfoCard
import org.saudigitus.semis.core.designsystem.components.filters.FilterInfoItem

/**
 * Filter context the attendance list is scoped to. Grade and class are only rendered
 * when the program configuration provides them.
 */
@Composable
internal fun AttendanceFilterInfo(
    state: FilterDetailsState,
    modifier: Modifier = Modifier,
) {
    FilterInfoCard(
        modifier = modifier,
        items = listOf(
            FilterInfoItem(
                label = stringResource(R.string.academic_year),
                value = state.academicYear,
                icon = Icons.Default.CalendarMonth,
            ),
            FilterInfoItem(
                label = stringResource(R.string.school),
                value = state.orgUnit,
                icon = Icons.Rounded.School,
            ),
            FilterInfoItem(
                label = stringResource(R.string.grade),
                value = state.grade.orEmpty(),
                icon = Icons.Rounded.Stairs,
            ),
            FilterInfoItem(
                label = stringResource(R.string.cls),
                value = state.section.orEmpty(),
                icon = Icons.Default.Groups,
            ),
        ),
    )
}
