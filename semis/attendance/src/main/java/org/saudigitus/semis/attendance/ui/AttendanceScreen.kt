package org.saudigitus.semis.attendance.ui

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.saudigitus.semis.attendance.ui.components.AttendanceBulkBar
import org.saudigitus.semis.attendance.ui.components.AttendanceHeader
import org.saudigitus.semis.attendance.ui.components.AttendanceSaveBar
import org.saudigitus.semis.attendance.ui.components.AttendanceStudentCard
import org.saudigitus.semis.attendance.ui.model.BottomSheetConfirmAction
import org.saudigitus.semis.attendance.ui.model.BottomSheetType
import org.saudigitus.semis.attendance.ui.model.attendanceStatTiles
import org.saudigitus.semis.core.designsystem.R
import org.saudigitus.semis.core.designsystem.components.AlertDialog
import org.saudigitus.semis.core.designsystem.components.ConfigNotFound
import org.saudigitus.semis.core.designsystem.components.NoResults
import org.saudigitus.semis.core.designsystem.components.SnackBar
import org.saudigitus.semis.core.designsystem.components.notice.InlineNotice
import org.saudigitus.semis.core.designsystem.templates.RoundedHeaderScaffold
import org.saudigitus.semis.core.designsystem.theme.dark_warning
import org.saudigitus.semis.core.designsystem.theme.light_error
import org.saudigitus.semis.core.designsystem.theme.light_success
import org.saudigitus.semis.core.form.data.model.FormType
import org.saudigitus.semis.core.form.ui.FormContent
import org.saudigitus.semis.core.form.ui.state.FormEvent
import org.saudigitus.semis.core.form.ui.state.FormUiState
import org.saudigitus.semis.core.utils.ButtonStep
import org.saudigitus.semis.attendance.R as AttendanceRes

@Composable
fun AttendanceScreen(
    state: AttendanceUiState,
    formState: FormUiState,
    snackbarHostState: SnackbarHostState,
    snackbarIsError: Boolean,
    onFormEvent: (FormEvent) -> Unit,
    onEvent: (AttendanceUiEvent) -> Unit,
) {
    if (state.displayDialog) {
        AlertDialog(
            message = stringResource(id = R.string.save_alert),
            onConfirm = {
                onEvent(AttendanceUiEvent.BottomSheetAction(BottomSheetConfirmAction.PERFORM_SAVE))
            }
        )
    }

    if (state.overrideBulk) {
        AlertDialog(
            message = stringResource(id = R.string.override_attendance),
            onConfirm = {
                onEvent(AttendanceUiEvent.BulkOverrideAttendance)
            }
        )
    }

    val isEditing = state.buttonStep != ButtonStep.NONE

    /**
     * A status is only meaningful once the day carries an attendance record, or while an
     * attendance is being taken. Until then the counters report zero, the learners are
     * listed without a status and the screen states that nothing was recorded.
     */
    val hasAttendanceForDay = state.hasAttendanceRecord || isEditing

    RoundedHeaderScaffold(
        header = {
            AttendanceHeader(
                headers = state.toolbarHeaders,
                tiles = attendanceStatTiles(
                    totalLabel = stringResource(AttendanceRes.string.attendance_total),
                    totalLearners = state.teis.size,
                    summaries = state.attendanceSummaryState.bottomSheetModels,
                    hasAttendanceRecord = state.hasAttendanceRecord,
                ),
                pendingSyncCount = state.pendingSyncCount,
                filterDetailsState = state.attendanceSummaryState.filterDetailsState,
                dateValidator = { state.dateValidator(it) },
                onNavigateBack = { onEvent(AttendanceUiEvent.NavBack) },
                onSync = { onEvent(AttendanceUiEvent.OnSyncClicked) },
                onDateSelected = { onEvent(AttendanceUiEvent.OnDateSelect(it)) },
            )
        },
        bottomBar = {
            AttendanceSaveBar(
                label = when {
                    !isEditing -> stringResource(R.string.take_attendance)
                    !state.allowAttendanceStatus -> stringResource(R.string.save)
                    state.hasPersistedAttendance -> stringResource(R.string.update_attendance)
                    else -> stringResource(R.string.complete_attendance)
                },
                imageVector = if (isEditing) Icons.Default.Save else Icons.Default.Edit,
                enabled = state.canTakeAttendance,
                onReset = if (isEditing) {
                    { onEvent(AttendanceUiEvent.ResetForm) }
                } else {
                    null
                },
                onClick = {
                    when {
                        !state.canTakeAttendance -> Unit
                        isEditing -> {
                            onEvent(AttendanceUiEvent.ShowBottomSheet(BottomSheetType.SUMMARY))
                        }

                        else -> onEvent(AttendanceUiEvent.OnEditClicked)
                    }
                },
            )
        },
        snackbarHost = {
            SnackBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                hostState = snackbarHostState,
                containerColor = if (snackbarIsError) light_error else light_success,
                painter = painterResource(
                    if (snackbarIsError) {
                        R.drawable.ic_outline_error_36
                    } else {
                        R.drawable.success_icon
                    }
                ),
            )
        },
    ) {
        if (isEditing) {
            AttendanceBulkBar(
                buttons = formState.attendanceButtonState.buttons,
                enabled = state.attendanceSummaryState.enableBulk,
                onBulk = { onEvent(AttendanceUiEvent.PerformBulk(it)) },
            )
        }

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
            when {
                formState.attendanceButtonState.buttons.isEmpty() && !formState.isLoading -> {
                    ConfigNotFound(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(horizontal = 16.dp),
                        iconSize = 32.dp,
                        message = stringResource(id = R.string.app_not_properly_config)
                    )
                }

                !state.canTakeAttendance -> {
                    InlineNotice(
                        text = stringResource(R.string.cannot_take_attendance),
                        imageVector = Icons.Default.Warning,
                        tone = dark_warning,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }

                !hasAttendanceForDay -> {
                    InlineNotice(
                        text = stringResource(AttendanceRes.string.attendance_not_recorded),
                        imageVector = Icons.Default.EventBusy,
                        tone = dark_warning,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 4.dp,
                    end = 16.dp,
                    bottom = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Top),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(state.teis, key = { it.tei.uid() }) { tei ->
                    AttendanceStudentCard(
                        learner = tei,
                        attendanceButtonState = formState.attendanceButtonState,
                        modifier = Modifier.testTag("TEI_ITEM"),
                        showStatusSelector = hasAttendanceForDay,
                        onStatusSelect = { buttonModel ->
                            if (state.allowAttendanceStatus) {
                                onFormEvent(FormEvent.UpdateAttendance(tei, buttonModel))
                            } else {
                                onEvent(
                                    AttendanceUiEvent.OnAttendanceClick(
                                        tei = tei,
                                        buttonModel = buttonModel,
                                    )
                                )
                            }
                        },
                        reasonContent = {
                            FormContent(
                                key = tei.uid(),
                                tei = tei,
                                type = FormType.ATTENDANCE,
                                modifier = Modifier.fillMaxWidth(),
                                state = formState,
                                fieldFilter = { it.isAttendanceReason },
                                onEvent = onFormEvent,
                            )
                        },
                    )
                }
            }
        }
    }
}
