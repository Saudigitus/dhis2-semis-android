package org.saudigitus.semis.app.presentation.tei

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItem
import org.hisp.dhis.mobile.ui.designsystem.component.ListCard
import org.hisp.dhis.mobile.ui.designsystem.component.ListCardDescriptionModel
import org.hisp.dhis.mobile.ui.designsystem.component.ListCardTitleModel
import org.hisp.dhis.mobile.ui.designsystem.component.state.rememberAdditionalInfoColumnState
import org.hisp.dhis.mobile.ui.designsystem.component.state.rememberListCardState
import org.saudigitus.semis.app.R
import org.saudigitus.semis.app.presentation.home.HomeUIState
import org.saudigitus.semis.core.designsystem.components.FilterDetails
import org.saudigitus.semis.core.designsystem.components.NoResults
import org.saudigitus.semis.core.designsystem.components.model.ToolbarHeaders
import org.saudigitus.semis.core.designsystem.templates.TopAppBarScaffold
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
        FilterDetails(
            modifier = Modifier.fillMaxWidth(),
            state = state.filterState.filterDetailsState,
            onClick = {}
        )

        if (state.tei.isEmpty()) {
            NoResults(message = stringResource(id = R.string.no_records_found))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(state.tei, key = { it.tei.uid() }) { tei ->
                    val card = searchTeiMapper(
                        tei = tei,
                        teiCardMapper = teiCardMapper,
                        onImageClick = { onEvent(TeiListEvent.DisplayImageDetail(it)) },
                        onCardClick = { tei, enrollment ->
                            onEvent(
                                TeiListEvent.OnTeiClick(
                                    tei,
                                    enrollment
                                )
                            )
                        }
                    )

                    ListCard(
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .testTag("TEI_ITEM"),
                        listCardState = rememberListCardState(
                            title = ListCardTitleModel(
                                text = card.first.title,
                                allowOverflow = false
                            ),
                            description = card.first.description?.let {
                                ListCardDescriptionModel(
                                    text = it,
                                )
                            },
                            lastUpdated = card.first.lastUpdated,
                            additionalInfoColumnState = rememberAdditionalInfoColumnState(
                                additionalInfoList = card.first.additionalInfo,
                                syncProgressItem = AdditionalInfoItem(
                                    key = stringResource(id = R.string.syncing),
                                    value = "",
                                ),
                                expandLabelText = stringResource(id = R.string.show_more),
                                shrinkLabelText = stringResource(id = R.string.show_less),
                                scrollableContent = true,
                            ),
                        ),
                        onCardClick = card.first.onCardCLick,
                        listAvatar = card.first.avatar,
                    )
                }
            }
        }
    }
}