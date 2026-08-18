package org.saudigitus.semis.performance.programstagedataelement

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Book
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.saudigitus.semis.core.designsystem.components.ConfigNotFound
import org.saudigitus.semis.core.designsystem.components.SemisFilterDetails
import org.saudigitus.semis.core.designsystem.components.ToolbarActionState
import org.saudigitus.semis.core.designsystem.components.cards.NavigationCard
import org.saudigitus.semis.core.designsystem.templates.TopAppBarScaffold
import org.saudigitus.semis.core.designsystem.theme.semisScreenBackground
import org.saudigitus.semis.performance.R
import org.saudigitus.semis.performance.route.Destinations.EVENT_CAPTURE

@Composable
fun ProgramStageDataElementsScreen(
    state: ProgramStageDataElementsUiState,
    navTo: (route: String) -> Unit = {},
    navBack: () -> Unit = {},
) {
    TopAppBarScaffold(
        toolbarHeaders = state.toolbarHeaders,
        navigationAction = navBack,
        toolbarActionState = ToolbarActionState(filterVisibility = false)
    ) {
        Column(
            modifier = Modifier.semisScreenBackground(),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
        ) {
            SemisFilterDetails(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp),
                state = state.filterState.filterDetailsState,
                showChevron = false,
            )

            if (state.programStageDataElements.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 4.dp,
                        bottom = 24.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Top),
                ) {
                    items(state.programStageDataElements) { item ->
                        NavigationCard(
                            title = item.dataElement?.displayName().orEmpty(),
                            icon = Icons.Rounded.Book,
                            enabled = state.filterState.filterDetailsState.count != 0,
                            onClick = {
                                navTo(
                                    "$EVENT_CAPTURE/${item.programStageUid}/${item.dataElement?.uid()}"
                                )
                            }
                        )
                    }
                }
            } else {
                ConfigNotFound(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    message = stringResource(R.string.performance_config_error)
                )
            }
        }
    }
}
