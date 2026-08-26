package org.saudigitus.semis.core.designsystem.filters

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.rounded.FilterAltOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.saudigitus.semis.core.designsystem.R
import org.saudigitus.semis.core.designsystem.components.buttons.RoundedSyncButton
import org.saudigitus.semis.core.designsystem.components.fields.DropDown
import org.saudigitus.semis.core.designsystem.components.fields.OuField
import org.saudigitus.semis.core.designsystem.components.model.FilterType

@Composable
fun FilterContainer(
    modifier: Modifier = Modifier,
    program: String,
    state: FilterComponentState,
    onEvent: (FilterComponentEvent) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
        horizontalAlignment = Alignment.Start,
    ) {
        state.academicYear?.let {
            DropDown(
                dropdownState = it,
                defaultSelection = state.selectedFilters
                    .getOrElse(FilterType.ACADEMIC_YEAR) { null },
                onItemClick = { item ->
                    onEvent(
                        FilterComponentEvent.FilterValueChange(
                            FilterType.ACADEMIC_YEAR,
                            item
                        )
                    )
                }
            )
        }
        OuField(
            placeholder = stringResource(R.string.school),
            leadingIcon = ImageVector.vectorResource(R.drawable.ic_location_on),
            selectedOrgUnit = state.orgUnit,
            program = program,
            onItemClick = {
                onEvent(FilterComponentEvent.FilterValueChange(FilterType.SCHOOL, it))
            }
        )
        AnimatedVisibility(visible = state.filters.isNotEmpty()) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
                horizontalAlignment = Alignment.Start,
            ) {
                items(state.filters) { filter ->
                    DropDown(
                        dropdownState = filter,
                        defaultSelection = state.selectedFilters
                            .getOrElse(filter.filterType) { null },
                        onItemClick = { item ->
                            onEvent(
                                FilterComponentEvent.FilterValueChange(
                                    filter.filterType,
                                    item
                                )
                            )
                        }
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ResetFiltersButton(
                modifier = Modifier.weight(RESET_WEIGHT),
                onClick = { onEvent(FilterComponentEvent.ResetFilters) },
            )
            RoundedSyncButton(
                modifier = Modifier.weight(DOWNLOAD_WEIGHT),
                label = stringResource(R.string.filters_download_records),
                leadingIcon = Icons.Default.Download,
                onClick = {
                    onEvent(FilterComponentEvent.Sync)
                }
            )
        }
    }
}

/**
 * Clears the class the user is on.
 *
 * Drawn as the quieter of the two actions on its line, because clearing what was chosen is a way
 * back rather than a step forward, and it sits beside an action that is the point of the screen.
 */
@Composable
private fun ResetFiltersButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    OutlinedButton(
        modifier = modifier.height(54.dp),
        onClick = onClick,
        shape = RoundedCornerShape(30.dp),
        contentPadding = PaddingValues(horizontal = 8.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White,
        ),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier.size(16.dp),
                imageVector = Icons.Rounded.FilterAltOff,
                contentDescription = null,
            )
            Text(
                text = stringResource(R.string.filters_reset),
                fontSize = 12.sp,
                maxLines = 1,
            )
        }
    }
}

/**
 * How the line is divided between the two actions.
 *
 * The clearing action takes the smaller share, being the way back rather than the point of the
 * screen, but enough of it to name what it clears instead of leaving the user to guess from an
 * icon.
 */
private const val RESET_WEIGHT = 2f
private const val DOWNLOAD_WEIGHT = 3f