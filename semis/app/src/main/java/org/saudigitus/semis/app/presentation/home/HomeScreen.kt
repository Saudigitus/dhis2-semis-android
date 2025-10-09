package org.saudigitus.semis.app.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.saudigitus.semis.app.presentation.navigation.AppRoutes
import org.saudigitus.semis.core.designsystem.components.ConfigNotFoud
import org.saudigitus.semis.core.designsystem.components.FilterDetails
import org.saudigitus.semis.core.designsystem.components.NoRecordsFound
import org.saudigitus.semis.core.designsystem.components.cards.ActionCard
import org.saudigitus.semis.core.designsystem.filters.FilterComponentEvent
import org.saudigitus.semis.core.designsystem.filters.FilterContainer
import org.saudigitus.semis.core.designsystem.templates.Backdrop
import org.saudigitus.semis.core.designsystem.utils.ModuleIcons

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    state: HomeUIState,
    onFilterEvent: (FilterComponentEvent) -> Unit,
    navTo: (route: String) -> Unit = {}
) {
    Backdrop(
        modifier = modifier,
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
        FilterDetails(
            modifier = Modifier.fillMaxWidth(),
            state = state.filterState.filterDetailsState,
            onClick = {
                navTo.invoke(AppRoutes.TRACKER_LIST)
            }
        )

        if (state.filterState.filterDetailsState.count == 0) {
            NoRecordsFound(
                modifier = Modifier.fillMaxWidth()
                    .padding(16.dp),
            )
        }

        if (state.modules.isNotEmpty()) {
            LazyVerticalGrid(
                modifier = Modifier.fillMaxSize(),
                columns = GridCells.Adaptive(128.dp),
                contentPadding = PaddingValues(vertical = 5.dp, horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
                horizontalArrangement = Arrangement.spacedBy(
                    16.dp,
                    Alignment.CenterHorizontally,
                ),
            ) {
                items(state.modules, key = { it.key }) {
                    ActionCard(
                        modifier = Modifier.fillMaxWidth(),
                        label = it.title,
                        enabled = state.filterState.filterDetailsState.count != 0,
                        icon = painterResource(ModuleIcons.getModuleIconByName(it.icon)),
                        onClick = {
                            navTo.invoke(it.route)
                        }
                    )
                }
            }
        } else {
            ConfigNotFoud(Modifier.fillMaxWidth().padding(horizontal = 16.dp))
        }
    }
}