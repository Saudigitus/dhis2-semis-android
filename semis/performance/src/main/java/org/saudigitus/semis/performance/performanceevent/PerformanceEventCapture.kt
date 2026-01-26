package org.saudigitus.semis.performance.performanceevent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import org.dhis2.ui.theme.colorPrimary
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItem
import org.hisp.dhis.mobile.ui.designsystem.component.ListCard
import org.hisp.dhis.mobile.ui.designsystem.component.ListCardDescriptionModel
import org.hisp.dhis.mobile.ui.designsystem.component.ListCardTitleModel
import org.hisp.dhis.mobile.ui.designsystem.component.state.rememberAdditionalInfoColumnState
import org.hisp.dhis.mobile.ui.designsystem.component.state.rememberListCardState
import org.hisp.dhis.mobile.ui.designsystem.theme.Radius
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.hisp.dhis.mobile.ui.designsystem.theme.dropShadow
import org.saudigitus.semis.core.designsystem.R
import org.saudigitus.semis.core.designsystem.components.AlertDialog
import org.saudigitus.semis.core.designsystem.components.NoResults
import org.saudigitus.semis.core.designsystem.components.SnackBar
import org.saudigitus.semis.core.designsystem.components.ToolbarActionState
import org.saudigitus.semis.core.designsystem.components.summary.SummaryDetails
import org.saudigitus.semis.core.designsystem.templates.TopAppBarScaffold
import org.saudigitus.semis.core.designsystem.utils.mapper.TEICardMapper
import org.saudigitus.semis.core.designsystem.utils.mapper.searchTeiMapper
import org.saudigitus.semis.core.form.data.model.FormType
import org.saudigitus.semis.core.form.ui.FormContent
import org.saudigitus.semis.core.form.ui.state.FormEvent
import org.saudigitus.semis.core.form.ui.state.FormUiState
import org.saudigitus.semis.core.utils.ButtonStep

@Composable
internal fun PerformanceEventCapture(
    state: PerformanceUiState,
    formState: FormUiState,
    teiCardMapper: TEICardMapper,
    snackbarHostState: SnackbarHostState,
    onEvent: (PerformanceUiEvent) -> Unit,
    onFormEvent: (FormEvent) -> Unit,
) {

    if (state.isConfirmDialog) {
        AlertDialog(
            message = stringResource(id = R.string.save_alert),
            onDismissRequest = { onEvent(PerformanceUiEvent.CancelEventData) },
            onConfirm = {
                onEvent(PerformanceUiEvent.SaveEvent)
            }
        )
    }

    TopAppBarScaffold(
        toolbarHeaders = state.toolbarHeaders,
        toolbarActionState = ToolbarActionState(
            filterVisibility = false,
            showCalendar = false
        ),
        navigationAction = { onEvent(PerformanceUiEvent.NavBack)  },
        syncAction = { onEvent(PerformanceUiEvent.Sync) },
        snackbarHost = {
            SnackBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                hostState = snackbarHostState,
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = {
                    Text(
                        text = if (state.buttonStep == ButtonStep.NONE) {
                            stringResource(R.string.update)
                        } else {
                            stringResource(R.string.save)
                        },
                        color = colorPrimary,
                        style = LocalTextStyle.current.copy(
                            fontFamily = FontFamily(Font(R.font.rubik_medium)),
                        ),
                    )
                },
                icon = {
                    Icon(
                        imageVector = if (state.buttonStep == ButtonStep.NONE) {
                            Icons.Default.Edit
                        } else {
                            Icons.Default.Save
                        },
                        contentDescription = null,
                        tint = colorPrimary,
                    )
                },
                onClick = {
                    if (state.buttonStep == ButtonStep.NONE) {
                        onEvent(PerformanceUiEvent.EditEvent)
                    } else {
                        onEvent(PerformanceUiEvent.ConfirmEventData)
                    }
                },
            )
        }
    ) {
        SummaryDetails(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
                .dropShadow(RoundedCornerShape(Radius.S))
                .background(
                    color = SurfaceColor.SurfaceBright,
                    shape = RoundedCornerShape(Radius.S)
                ),
            state = state.summaryState,
        )

        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (state.tei.isEmpty()) {
            NoResults(message = stringResource(id = R.string.no_records_found))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 10.dp,
                    end = 16.dp,
                    bottom = 108.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(state.tei, key = { it.tei.uid() }) { tei ->
                    val card = searchTeiMapper(
                        tei = tei,
                        teiCardMapper = teiCardMapper,
                        onImageClick = { },
                        onCardClick = { _, _ ->

                        }
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .dropShadow(RoundedCornerShape(Radius.S))
                            .background(
                                color = SurfaceColor.SurfaceBright,
                                shape = RoundedCornerShape(Radius.S)
                            ),
                        verticalArrangement = Arrangement.spacedBy(
                            12.dp,
                            Alignment.CenterVertically
                        ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
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
                                expandable = false,
                                shadow = false,
                                additionalInfoColumnState = rememberAdditionalInfoColumnState(
                                    additionalInfoList = card.first.additionalInfo,
                                    syncProgressItem = AdditionalInfoItem(
                                        key = stringResource(id = R.string.syncing),
                                        value = "",
                                    ),
                                    minItemsToShow = 2,
                                    expandLabelText = stringResource(id = R.string.show_more),
                                    shrinkLabelText = stringResource(id = R.string.show_less),
                                    scrollableContent = false,
                                ),
                            ),
                            onCardClick = card.first.onCardCLick,
                            listAvatar = card.first.avatar,
                        )
                        FormContent(
                            key = tei.uid(),
                            tei = tei,
                            type = FormType.INDIVIDUAL,
                            modifier = Modifier.fillMaxWidth(),
                            state = formState,
                            onEvent = onFormEvent
                        )
                        Spacer(modifier = Modifier.padding(2.dp))
                    }
                }
            }
        }
    }
}