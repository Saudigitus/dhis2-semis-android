package org.saudigitus.semis.core.designsystem.filters

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
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
        RoundedSyncButton(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            label = stringResource(R.string.sync),
            leadingIcon = Icons.Default.Download,
            onClick = {
                onEvent(FilterComponentEvent.Sync)
            }
        )
    }
}