package org.saudigitus.semis.attendance.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
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
import org.saudigitus.semis.attendance.ui.model.ButtonStep
import org.saudigitus.semis.core.designsystem.R
import org.saudigitus.semis.core.designsystem.attendance.AttendanceButton
import org.saudigitus.semis.core.designsystem.components.ConfigNotFound
import org.saudigitus.semis.core.designsystem.components.FilterDetails
import org.saudigitus.semis.core.designsystem.components.NoResults
import org.saudigitus.semis.core.designsystem.components.SnackBar
import org.saudigitus.semis.core.designsystem.components.ToolbarActionState
import org.saudigitus.semis.core.designsystem.components.bottomsheet.SummaryBottomSheet
import org.saudigitus.semis.core.designsystem.templates.TopAppBarScaffold
import org.saudigitus.semis.core.designsystem.utils.mapper.TEICardMapper
import org.saudigitus.semis.core.designsystem.utils.mapper.searchTeiMapper

@Composable
fun AttendanceScreen(
    state: AttendanceUiState,
    snackbarHostState: SnackbarHostState,
    teiCardMapper: TEICardMapper,
    onEvent: (AttendanceUiEvent) -> Unit,
) {
    if (state.displaySummary) {
        SummaryBottomSheet(
            state = state.bottomSheetState,
            onDismissRequest = { onEvent(AttendanceUiEvent.DismissBottomSheet) },
            onSave = { onEvent(AttendanceUiEvent.OnSaveClicked) },
        )
    }

    TopAppBarScaffold(
        toolbarHeaders = state.toolbarHeaders,
        toolbarActionState = ToolbarActionState(
            filterVisibility = false,
            showCalendar = true
        ),
        navigationAction = { onEvent(AttendanceUiEvent.NavBack) },
        syncAction = { onEvent(AttendanceUiEvent.OnSyncClicked) },
        calendarAction = { onEvent(AttendanceUiEvent.OnDateSelect(it)) },
        dateValidator = { state.dateValidator(it) },
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
                        onEvent(AttendanceUiEvent.OnEditClicked)
                    } else {
                        onEvent(AttendanceUiEvent.ShowBottomSheet)
                    }
                },
            )
        }
    ) {
        FilterDetails(
            modifier = Modifier.fillMaxWidth(),
            state = state.filterDetailsState,
            onClick = {}
        )

        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (state.teis.isEmpty()) {
            NoResults(message = stringResource(id = R.string.no_records_found))
        } else {
            if (state.attendanceButtonState.buttons.isEmpty() && !state.attendanceButtonState.isLoading) {
                ConfigNotFound(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(horizontal = 16.dp),
                    iconSize = 32.dp,
                    message = stringResource(id = R.string.app_not_properly_config)
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, top = 10.dp, bottom = 108.dp)
            ) {
                items(state.teis, key = { it.tei.uid() }) { tei ->
                    val card = searchTeiMapper(
                        tei = tei,
                        teiCardMapper = teiCardMapper,
                        onImageClick = { },
                        onCardClick = { _, _ ->

                        }
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(color = Color.White),
                        contentAlignment = Alignment.CenterEnd,
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
                        AttendanceButton(
                            key = tei.uid(),
                            modifier = Modifier.padding(horizontal = 16.dp),
                            state = state.attendanceButtonState,
                            onClick = { model ->
                                onEvent(
                                    AttendanceUiEvent.OnAttendanceClick(
                                        tei = tei,
                                        buttonModel = model
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}