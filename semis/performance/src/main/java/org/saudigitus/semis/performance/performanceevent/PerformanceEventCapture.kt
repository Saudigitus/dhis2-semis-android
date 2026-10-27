package org.saudigitus.semis.performance.performanceevent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.saudigitus.semis.core.designsystem.R
import org.saudigitus.semis.core.designsystem.components.AlertDialog
import org.saudigitus.semis.core.designsystem.components.NoResults
import org.saudigitus.semis.core.designsystem.components.SnackBar
import org.saudigitus.semis.core.designsystem.components.ToolbarActionState
import org.saudigitus.semis.core.designsystem.templates.TopAppBarScaffold
import org.saudigitus.semis.core.designsystem.theme.semisScreenBackground
import org.saudigitus.semis.core.form.ui.state.FormEvent
import org.saudigitus.semis.core.form.ui.state.FormUiState
import org.saudigitus.semis.core.form.utils.FactoryData
import org.saudigitus.semis.core.utils.ButtonStep
import org.saudigitus.semis.performance.performanceevent.components.MissingMarksBanner
import org.saudigitus.semis.performance.performanceevent.components.PerformanceMarksHeader
import org.saudigitus.semis.performance.performanceevent.components.PerformanceSaveBar
import org.saudigitus.semis.performance.performanceevent.components.PerformanceStudentRow
import org.saudigitus.semis.performance.performanceevent.components.marksStats

@Composable
internal fun PerformanceEventCapture(
    state: PerformanceUiState,
    formState: FormUiState,
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

    val renderedFields = formState.fields.filter { it.rendered }

    // The compact row only fits a single numeric mark, any other form keeps the full width one.
    val markField = renderedFields.singleOrNull()
        ?.takeIf { FactoryData.NUMERIC_TYPES.contains(it.valueType) }

    val stats = marksStats(
        learners = state.tei,
        dataElement = markField?.dataElementUid,
        fieldsData = formState.fieldsData,
    )
    val isEditing = state.buttonStep != ButtonStep.NONE
    val filterDetails = state.summaryState.filterDetailsState

    TopAppBarScaffold(
        toolbarHeaders = state.toolbarHeaders,
        toolbarActionState = ToolbarActionState(
            filterVisibility = false,
            showCalendar = false
        ),
        navigationAction = { onEvent(PerformanceUiEvent.NavBack) },
        syncAction = { onEvent(PerformanceUiEvent.Sync) },
        snackbarHost = {
            SnackBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                hostState = snackbarHostState,
            )
        },
        bottomBar = {
            PerformanceSaveBar(isEditing = isEditing) {
                if (isEditing) {
                    onEvent(PerformanceUiEvent.ConfirmEventData)
                } else {
                    onEvent(PerformanceUiEvent.EditEvent)
                }
            }
        },
    ) {
        Column(modifier = Modifier.semisScreenBackground()) {
            PerformanceMarksHeader(
                title = renderedFields.firstOrNull()?.label ?: state.toolbarHeaders.title,
                context = listOfNotNull(
                    filterDetails.grade?.takeIf { it.isNotEmpty() },
                    filterDetails.section?.takeIf { it.isNotEmpty() },
                    filterDetails.academicYear.takeIf { it.isNotEmpty() },
                ).joinToString(separator = " · "),
                stats = stats,
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
                        top = 12.dp,
                        end = 16.dp,
                        bottom = 24.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Top),
                ) {
                    if (stats.missing > 0) {
                        item(key = "missing_marks") {
                            MissingMarksBanner(missing = stats.missing)
                        }
                    }

                    items(state.tei, key = { it.tei.uid() }) { learner ->
                        PerformanceStudentRow(
                            learner = learner,
                            formState = formState,
                            markField = markField,
                            modifier = Modifier.testTag("TEI_ITEM"),
                            onFormEvent = onFormEvent,
                        )
                    }
                }
            }
        }
    }
}
