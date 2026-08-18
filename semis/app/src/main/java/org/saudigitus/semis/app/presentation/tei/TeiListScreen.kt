package org.saudigitus.semis.app.presentation.tei

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.saudigitus.semis.app.R
import org.saudigitus.semis.app.presentation.home.HomeUIState
import org.saudigitus.semis.core.designsystem.components.NoResults
import org.saudigitus.semis.core.designsystem.components.SemisFilterDetails
import org.saudigitus.semis.core.designsystem.components.cards.TeiLearnerCard
import org.saudigitus.semis.core.designsystem.components.model.ToolbarHeaders
import org.saudigitus.semis.core.designsystem.templates.TopAppBarScaffold
import org.saudigitus.semis.core.designsystem.theme.semisScreenBackground
import org.saudigitus.semis.core.designsystem.utils.mapper.TEICardMapper
import org.saudigitus.semis.core.designsystem.utils.mapper.searchTeiMapper

@Composable
fun TeiListScreen(
    state: HomeUIState,
    teiCardMapper: TEICardMapper,
    onEvent: (TeiListEvent) -> Unit
) {
    TopAppBarScaffold(
        toolbarHeaders = ToolbarHeaders(
            title = state.programName,
        ),
        navigationAction = { onEvent(TeiListEvent.OnBack) },
        syncAction = { onEvent(TeiListEvent.OnSyncClick) }
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

            if (state.tei.isEmpty()) {
                NoResults(message = stringResource(id = R.string.no_records_found))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        top = 4.dp,
                        end = 16.dp,
                        bottom = 108.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
                ) {
                    items(state.tei, key = { it.tei.uid() }) { tei ->
                        val card = searchTeiMapper(
                            tei = tei,
                            teiCardMapper = teiCardMapper,
                            onImageClick = { onEvent(TeiListEvent.DisplayImageDetail(it)) },
                            onCardClick = { teiUid, enrollment ->
                                onEvent(TeiListEvent.OnTeiClick(teiUid, enrollment))
                            }
                        )

                        TeiLearnerCard(
                            tei = tei,
                            modifier = Modifier.testTag("TEI_ITEM"),
                            additionalInfo = card.first.additionalInfo,
                            onClick = card.first.onCardCLick,
                        )
                    }
                }
            }
        }
    }
}
