package org.saudigitus.semis.app.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.saudigitus.semis.core.designsystem.filters.FilterComponentEvent
import org.saudigitus.semis.core.designsystem.filters.FilterContainer
import org.saudigitus.semis.core.designsystem.templates.Backdrop

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    state: HomeUIState,
    onFilterEvent: (FilterComponentEvent) -> Unit,
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

        }
    }
}