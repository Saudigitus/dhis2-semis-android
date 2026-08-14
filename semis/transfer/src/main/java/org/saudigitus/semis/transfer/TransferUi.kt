package org.saudigitus.semis.transfer

import androidx.activity.compose.BackHandler
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
import org.saudigitus.semis.transfer.event.TransferUiEvent
import org.saudigitus.semis.transfer.model.TransferMessageType
import org.saudigitus.semis.transfer.model.TransferStep
import org.saudigitus.semis.core.form.data.model.FormType
import org.saudigitus.semis.core.form.ui.FormViewModel
import org.saudigitus.semis.core.form.ui.state.FormBuilderState
import org.saudigitus.semis.core.form.ui.state.FormEvent

@Composable
fun TransferUi(
    viewModel: TransferViewModel,
    formViewModel: FormViewModel,
    navigateBack: () -> Unit,
    syncData: () -> Unit,
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
        viewModel.syncEvent.collectLatest { syncData() }
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
                )
            )
            formViewModel.enableForm()
        }
    }

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
        state.originSchoolDataElement,
        state.sourceOrgUnit?.uid,
    ) {
        val sourceUid = state.sourceOrgUnit?.uid.orEmpty()
        val originField = formState.fields.find {
            it.dataElementUid == state.originSchoolDataElement
        }
        if (originField != null && originField.value != sourceUid) {
            formViewModel.handleUiEvent(
                FormEvent.UpdateField(
                    formType = FormType.DEFAULT,
                    tei = "",
                    dataElementUid = originField.dataElementUid,
                    value = sourceUid,
                )
            )
        }
    }

    LaunchedEffect(formState.fields, formState.isLoading, state.destinationSchoolDataElement) {
        val destinationField = formState.fields.find {
            it.dataElementUid == state.destinationSchoolDataElement
        }
        val isValid = !formState.isLoading && formState.fields
            .filter {
                it.rendered &&
                    it.enabled &&
                    it.mandatory &&
                    it.dataElementUid != state.originSchoolDataElement
            }
            .all { !it.value.isNullOrBlank() }
        viewModel.updateTransferForm(
            destinationOrgUnit = destinationField?.selectedOrgUnit,
            isValid = isValid,
        )
    }

    fun handleBack() {
        if (state.step == TransferStep.SELECT_LEARNERS) {
            navigateBack()
        } else {
            viewModel.handleEvent(TransferUiEvent.Back)
        }
    }

    BackHandler(onBack = ::handleBack)

    TransferScreen(
        state = state,
        formState = formState,
        snackbarHostState = snackbarHostState,
        snackbarMessageType = snackbarMessageType,
        onEvent = viewModel::handleEvent,
        onFormEvent = formViewModel::handleUiEvent,
        navigateBack = ::handleBack,
    )
}

private fun presetValue(key: String, value: String): Map<String, String> =
    if (key.isNotBlank() && value.isNotBlank()) mapOf(key to value) else emptyMap()
