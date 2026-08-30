package org.saudigitus.semis.transfer

import androidx.activity.compose.BackHandler
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import org.saudigitus.semis.core.data.model.SyncTarget
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
import org.saudigitus.semis.core.form.ui.FormViewModel
import org.saudigitus.semis.core.form.ui.state.FormBuilderState
import org.saudigitus.semis.transfer.event.TransferUiEvent
import org.saudigitus.semis.transfer.model.TransferMessageType

@Composable
fun TransferUi(
    viewModel: TransferViewModel,
    formViewModel: FormViewModel,
    navigateBack: () -> Unit,
    syncData: (List<SyncTarget>) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val formState by formViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessageType by remember { mutableStateOf(TransferMessageType.SUCCESS) }

    LaunchedEffect(Unit) {
        viewModel.messageEvent.collectLatest { message ->
            snackbarMessageType = message.type
            snackbarHostState.showSnackbar(
                message = message.text,
                duration = SnackbarDuration.Long,
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.syncEvent.collectLatest { targets -> syncData(targets) }
    }

    LaunchedEffect(Unit) {
        viewModel.formResetEvent.collectLatest { formViewModel.resetValues() }
    }

    LaunchedEffect(
        state.program,
        state.transferProgramStage,
        state.sourceOrgUnit?.uid,
        state.statusDataElement,
        state.pendingStatusCode,
    ) {
        val sourceOrgUnit = state.sourceOrgUnit ?: return@LaunchedEffect
        if (state.program.isNotBlank() && state.transferProgramStage.isNotBlank()) {
            formViewModel.initialize(
                FormBuilderState(
                    orgUnit = sourceOrgUnit.uid,
                    program = state.program,
                    programStage = state.transferProgramStage,
                    presetValues = presetValue(
                        state.statusDataElement,
                        state.pendingStatusCode,
                    ),
                    lockedDataElements = setOf(state.statusDataElement)
                        .filter(String::isNotBlank)
                        .toSet(),
                ),
            )
            formViewModel.enableForm()
        }
    }

    /**
     * The pending status is never shown, so it is kept applied rather than left to the
     * form: the request has to reach the server carrying it.
     */
    LaunchedEffect(
        formState.fields,
        formState.isLoading,
        state.statusDataElement,
        state.pendingStatusCode,
    ) {
        if (formState.isLoading) return@LaunchedEffect
        val statusField = formState.fields.find {
            it.dataElementUid == state.statusDataElement
        } ?: return@LaunchedEffect
        if (statusField.value != state.pendingStatusCode || statusField.enabled) {
            formViewModel.applyPresetValue(
                dataElementUid = statusField.dataElementUid,
                value = state.pendingStatusCode,
                locked = true,
            )
        }
    }

    LaunchedEffect(
        formState.fields,
        formState.isLoading,
        state.destinationSchoolDataElement,
        state.statusDataElement,
    ) {
        val destinationField = formState.fields.find {
            it.dataElementUid == state.destinationSchoolDataElement
        }
        val isValid = !formState.isLoading && formState.fields
            .filter {
                it.rendered &&
                    it.enabled &&
                    it.mandatory &&
                    it.dataElementUid != state.statusDataElement
            }
            .all { !it.value.isNullOrBlank() }
        // Everything the form collected travels with the request. The destination and
        // the status are set by the flow itself, so they are left out here.
        val values = formState.fields
            .filter { it.rendered }
            .filterNot { it.dataElementUid == state.statusDataElement }
            .filterNot { it.dataElementUid == state.destinationSchoolDataElement }
            .mapNotNull { field ->
                field.value?.takeIf { it.isNotBlank() }?.let { field.dataElementUid to it }
            }

        viewModel.updateRequestForm(
            destinationOrgUnit = destinationField?.selectedOrgUnit,
            isValid = isValid,
            values = values,
        )
    }

    fun handleBack() {
        if (state.isRequesting) {
            viewModel.handleUiEvent(TransferUiEvent.Back)
        } else {
            navigateBack()
        }
    }

    BackHandler(onBack = ::handleBack)

    TransferScreen(
        state = state,
        formState = formState,
        snackbarHostState = snackbarHostState,
        snackbarMessageType = snackbarMessageType,
        onEvent = viewModel::handleUiEvent,
        onFormEvent = formViewModel::handleUiEvent,
        navigateBack = ::handleBack,
    )
}

private fun presetValue(key: String, value: String): Map<String, String> =
    if (key.isNotBlank() && value.isNotBlank()) mapOf(key to value) else emptyMap()
