package org.saudigitus.semis.app.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.HourglassEmpty
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.saudigitus.semis.app.R
import org.saudigitus.semis.app.presentation.home.components.HomeAccent
import org.saudigitus.semis.app.presentation.home.components.HomeBackground
import org.saudigitus.semis.app.presentation.home.components.HomeModuleCard
import org.saudigitus.semis.app.presentation.home.components.HomeSectionLabel
import org.saudigitus.semis.app.presentation.home.components.HomeStat
import org.saudigitus.semis.app.presentation.home.components.HomeStatsRow
import org.saudigitus.semis.app.presentation.home.components.moduleAccent
import org.saudigitus.semis.app.presentation.navigation.AppRoutes
import org.saudigitus.semis.core.designsystem.components.ConfigNotFound
import org.saudigitus.semis.core.designsystem.components.NoRecordsFound
import org.saudigitus.semis.core.designsystem.components.SemisFilterDetails
import org.saudigitus.semis.core.designsystem.filters.FilterComponentEvent
import org.saudigitus.semis.core.designsystem.filters.FilterContainer
import org.saudigitus.semis.core.designsystem.templates.Backdrop
import org.saudigitus.semis.core.designsystem.utils.ModuleIcons
import org.saudigitus.semis.core.designsystem.R as DesignSystemR

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    state: HomeUIState,
    onFilterEvent: (FilterComponentEvent) -> Unit,
    navTo: (route: String) -> Unit = {}
) {
    Backdrop(
        modifier = modifier,
        frontLayerContainerColor = HomeBackground,
        backLayer = {
            AnimatedVisibility(visible = state.displayFilters) {
                FilterContainer(
                    modifier = Modifier.padding(bottom = 16.dp),
                    program = state.program,
                    state = state.filterState,
                    onEvent = onFilterEvent
                )
            }
            if (state.isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
    ) {
        val filterDetails = state.filterState.filterDetailsState
        val stats = homeStats(state)

        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize(),
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            fullWidthItem(key = "filter_details") {
                SemisFilterDetails(
                    state = filterDetails,
                    onClick = {
                        navTo.invoke(AppRoutes.TRACKER_LIST)
                    }
                )
            }

            if (filterDetails.count == 0) {
                fullWidthItem(key = "no_records") {
                    NoRecordsFound(
                        modifier = Modifier.fillMaxWidth(),
                        message = state.errorMessage
                            ?: stringResource(DesignSystemR.string.no_records_found)
                    )
                }
            }

            if (state.modules.isNotEmpty()) {
                fullWidthItem(key = "modules_label") {
                    HomeSectionLabel(text = stringResource(R.string.home_modules))
                }

                items(state.modules, key = { it.key }) { module ->
                    HomeModuleCard(
                        label = module.title,
                        icon = painterResource(ModuleIcons.getModuleIconByName(module.icon)),
                        accent = moduleAccent(module.icon),
                        enabled = module.enabled && filterDetails.count != 0,
                        onClick = {
                            navTo.invoke(module.route)
                        }
                    )
                }
            } else {
                fullWidthItem(key = "config_not_found") {
                    ConfigNotFound(Modifier.fillMaxWidth())
                }
            }
        }
    }
}

private fun LazyGridScope.fullWidthItem(
    key: String,
    content: @Composable () -> Unit,
) {
    item(key = key, span = { GridItemSpan(maxLineSpan) }) { content() }
}

@Composable
private fun homeStats(state: HomeUIState): List<HomeStat> {
    val count = state.filterState.filterDetailsState.count

    return listOf(
        HomeStat(
            value = "$count",
            label = stringResource(R.string.home_stat_enrolled),
            icon = Icons.Outlined.Groups,
            accent = HomeAccent.Blue,
        ),
        HomeStat(
            value = "${state.modules.size}",
            label = stringResource(R.string.home_stat_modules),
            icon = Icons.Outlined.Widgets,
            accent = HomeAccent.Purple,
        ),
        HomeStat(
            value = "12",
            label = "Pending",
            icon = Icons.Rounded.Warning,
            accent = HomeAccent.Amber,
        ),
    )
}
