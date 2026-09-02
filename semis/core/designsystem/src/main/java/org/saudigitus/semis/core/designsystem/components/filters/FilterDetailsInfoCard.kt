package org.saudigitus.semis.core.designsystem.components.filters

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Stairs
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.saudigitus.semis.core.designsystem.R
import org.saudigitus.semis.core.designsystem.components.FilterDetailsState
import org.saudigitus.semis.core.designsystem.theme.SemisPalette

/**
 * The selection a screen is scoped to — academic year, school, grade and class — rendered
 * over a colored header on the same translucent surface as the header actions.
 *
 * Grade and class are dropped when the program configuration does not provide them, so the
 * remaining entries widen instead of leaving gaps.
 */
@Composable
fun FilterDetailsInfoCard(
    state: FilterDetailsState,
    modifier: Modifier = Modifier,
    containerColor: Color = SemisPalette.HeaderSurface,
    accentColor: Color = Color.White,
    iconContainerColor: Color = SemisPalette.HeaderSurface,
    labelColor: Color = SemisPalette.OnHeaderSecondary,
    valueColor: Color = SemisPalette.OnHeaderPrimary,
    elevation: Dp = 0.dp,
) {
    FilterInfoCard(
        modifier = modifier,
        containerColor = containerColor,
        accentColor = accentColor,
        iconContainerColor = iconContainerColor,
        labelColor = labelColor,
        valueColor = valueColor,
        elevation = elevation,
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
