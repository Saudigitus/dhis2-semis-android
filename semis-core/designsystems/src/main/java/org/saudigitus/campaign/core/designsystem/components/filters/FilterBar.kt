package org.saudigitus.campaign.core.designsystem.components.filters

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import org.saudigitus.campaign.core.data.models.FilterModel
import org.saudigitus.campaign.core.data.models.OptionModel

@Composable
fun FilterBar(
    modifier: Modifier = Modifier,
    state: FilterBarState,
    onClick: (FilterModel, OptionModel?) -> Unit
) {
    AnimatedVisibility(state.display) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
            horizontalAlignment = Alignment.Start
        ) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(state.chipFilters(), key = { it.uid }) { filter ->
                    if (filter.optionModel.isEmpty()) {
                        FilterChip(
                            onClick = { onClick.invoke(filter, null) },
                            label = { Text(filter.displayName) },
                            selected = filter.value.toBoolean(),
                        )
                    } else {
                        FilterChipDropdown(
                            filter = filter,
                            onClick = { item1, item2 ->
                                onClick.invoke(item1, item2)
                            }
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(state.switchFilters(), key = { it.uid }) { filter ->
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clickable(
                                role = Role.Switch,
                                onClickLabel = filter.displayName,
                                onClick = { onClick.invoke(filter, null) },
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(filter.displayName)
                        Switch(
                            checked = filter.value.toBoolean(),
                            onCheckedChange = { onClick.invoke(filter, null) }
                        )
                    }
                }
            }
        }
    }
}