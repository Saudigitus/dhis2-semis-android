package org.saudigitus.semis.transfer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.dhis2.commons.resources.ResourceManager
import org.saudigitus.semis.core.data.model.OrgUnit
import org.saudigitus.semis.core.data.model.SearchTeiModel
import org.saudigitus.semis.core.data.model.transfer.TeiTransferLearner
import org.saudigitus.semis.core.data.model.transfer.TeiTransferRequest
import org.saudigitus.semis.core.data.repository.TeiTransferRepository
import org.saudigitus.semis.core.designsystem.components.FilterDetailsState
import org.saudigitus.semis.transfer.event.TransferUiEvent
import org.saudigitus.semis.transfer.model.TransferMessage
import org.saudigitus.semis.transfer.model.TransferMessageType
import org.saudigitus.semis.transfer.model.TransferStep
import org.saudigitus.semis.transfer.model.TransferTab
import org.saudigitus.semis.transfer.state.TransferUiState
import javax.inject.Inject

@HiltViewModel
class TransferViewModel @Inject constructor(
    private val repository: TeiTransferRepository,
    private val resourceManager: ResourceManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransferUiState())
    val uiState: StateFlow<TransferUiState> = _uiState.asStateFlow()

    private val _messageEvent = MutableSharedFlow<TransferMessage>()
    val messageEvent: SharedFlow<TransferMessage> = _messageEvent.asSharedFlow()

    private val _syncEvent = MutableSharedFlow<Unit>()
    val syncEvent: SharedFlow<Unit> = _syncEvent.asSharedFlow()

    private val _formResetEvent = MutableSharedFlow<Unit>()
    val formResetEvent: SharedFlow<Unit> = _formResetEvent.asSharedFlow()

    private var initialized = false

    fun initialize(
        program: String,
        sourceOrgUnit: OrgUnit,
        learners: List<SearchTeiModel>,
        originFilterDetails: FilterDetailsState,
    ) {
        if (initialized) return
        initialized = true

        _uiState.update {
            it.copy(
                program = program,
                sourceOrgUnit = sourceOrgUnit,
                learners = learners,
                originFilterDetails = originFilterDetails.copy(
                    enable = false,
                    enableCounter = false,
                ),
                isLoadingMetadata = true,
            )
        }
        loadTransferMetadata(program)
        loadIncomingTransfers()
        loadPendingOutgoingTransfers()
    }

    fun handleEvent(event: TransferUiEvent) {
        when (event) {
            is TransferUiEvent.SelectTab -> selectTab(event.tab)
            is TransferUiEvent.ToggleLearner -> toggleLearner(event.teiUid)
            is TransferUiEvent.ApproveIncoming -> approveIncoming(event.eventUid)
            TransferUiEvent.Continue -> continueFlow()
            TransferUiEvent.Back -> previousStep()
        }
    }

    fun updateTransferForm(destinationOrgUnit: OrgUnit?, isValid: Boolean) {
        _uiState.update {
            it.copy(
                destinationOrgUnit = destinationOrgUnit,
                isTransferFormValid = isValid,
            )
        }
    }

    private fun selectTab(tab: TransferTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    private fun loadTransferMetadata(program: String) {
        viewModelScope.launch {
            runCatching { repository.getTransferMetadata(program) }
                .onSuccess { metadata ->
                    _uiState.update {
                        it.copy(
                            isLoadingMetadata = false,
                            transferProgramStage = metadata.programStage,
                            originSchoolDataElement = metadata.originSchoolDataElement,
                            destinationSchoolDataElement = metadata.destinationSchoolDataElement,
                            statusDataElement = metadata.statusDataElement,
                            pendingStatusCode = metadata.pendingStatusCode,
                        )
                    }
                }.onFailure { error ->
                    _uiState.update { it.copy(isLoadingMetadata = false) }
                    emitError(
                        error.message
                            ?: resourceManager.getString(R.string.transfer_form_load_failed),
                    )
                }
        }
    }

    private fun loadIncomingTransfers() {
        val current = _uiState.value
        val orgUnit = current.sourceOrgUnit ?: return
        if (current.isLoadingIncoming) return

        _uiState.update { it.copy(isLoadingIncoming = true) }
        viewModelScope.launch {
            runCatching { repository.getIncomingTransfers(current.program, orgUnit.uid) }
                .onSuccess { incoming ->
                    _uiState.update {
                        it.copy(isLoadingIncoming = false, incomingTransfers = incoming)
                    }
                }.onFailure { error ->
                    _uiState.update { it.copy(isLoadingIncoming = false) }
                    emitError(
                        error.message
                            ?: resourceManager.getString(R.string.incoming_transfer_load_failed),
                    )
                }
        }
    }

    private fun loadPendingOutgoingTransfers() {
        val current = _uiState.value
        val orgUnit = current.sourceOrgUnit ?: return
        if (current.isLoadingPendingOutgoing) return

        _uiState.update { it.copy(isLoadingPendingOutgoing = true) }
        viewModelScope.launch {
            runCatching {
                repository.getPendingOutgoingTransfers(current.program, orgUnit.uid)
            }.onSuccess { pending ->
                _uiState.update {
                    it.copy(isLoadingPendingOutgoing = false, pendingOutgoingTransfers = pending)
                }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoadingPendingOutgoing = false) }
                emitError(
                    error.message
                        ?: resourceManager.getString(R.string.pending_outgoing_load_failed),
                )
            }
        }
    }

    private fun approveIncoming(eventUid: String) {
        val current = _uiState.value
        val orgUnit = current.sourceOrgUnit ?: return
        if (eventUid in current.approvingEventUids) return

        _uiState.update {
            it.copy(approvingEventUids = it.approvingEventUids + eventUid)
        }
        viewModelScope.launch {
            runCatching {
                repository.approveIncomingTransfer(
                    program = current.program,
                    currentOrgUnit = orgUnit.uid,
                    eventUid = eventUid,
                )
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        approvingEventUids = it.approvingEventUids - eventUid,
                        incomingTransfers = it.incomingTransfers.filterNot { request ->
                            request.eventUid == eventUid
                        },
                    )
                }
                emitSuccess(
                    resourceManager.getString(R.string.incoming_transfer_approved),
                )
                _syncEvent.emit(Unit)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(approvingEventUids = it.approvingEventUids - eventUid)
                }
                emitError(
                    error.message
                        ?: resourceManager.getString(R.string.incoming_transfer_approval_failed),
                )
            }
        }
    }

    private fun toggleLearner(teiUid: String) {
        _uiState.update { current ->
            val selected = current.selectedLearnerUids.toMutableSet()
            if (!selected.add(teiUid)) selected.remove(teiUid)
            current.copy(selectedLearnerUids = selected)
        }
    }

    private fun continueFlow() {
        val current = _uiState.value
        if (!current.canContinue) return

        when (current.step) {
            TransferStep.SELECT_LEARNERS -> _uiState.update {
                it.copy(step = TransferStep.DESTINATION)
            }
            TransferStep.DESTINATION -> _uiState.update {
                it.copy(step = TransferStep.REVIEW)
            }
            TransferStep.REVIEW -> submitTransfer()
        }
    }

    private fun previousStep() {
        _uiState.update { current ->
            current.copy(
                step = when (current.step) {
                    TransferStep.SELECT_LEARNERS -> TransferStep.SELECT_LEARNERS
                    TransferStep.DESTINATION -> TransferStep.SELECT_LEARNERS
                    TransferStep.REVIEW -> TransferStep.DESTINATION
                }
            )
        }
    }

    private fun submitTransfer() {
        val current = _uiState.value
        val destination = current.destinationOrgUnit ?: return
        val selectedLearners = current.learners
            .filter { it.tei.uid() in current.selectedLearnerUids }
            .mapNotNull { learner ->
                learner.selectedEnrollment?.uid()?.let { enrollmentUid ->
                    TeiTransferLearner(
                        teiUid = learner.tei.uid(),
                        enrollmentUid = enrollmentUid,
                    )
                }
            }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            runCatching {
                repository.transfer(
                    TeiTransferRequest(
                        program = current.program,
                        destinationOrgUnit = destination.uid,
                        learners = selectedLearners,
                        effectiveDate = current.effectiveDate,
                    )
                )
            }.onSuccess { result ->
                _uiState.update { state ->
                    state.copy(
                        isSubmitting = false,
                        selectedTab = TransferTab.TRANSFERS,
                        selectedLearnerUids = emptySet(),
                        learners = state.learners.filterNot {
                            it.tei.uid() in result.transferredTeiUids
                        },
                        destinationOrgUnit = null,
                        isTransferFormValid = false,
                        transferredCount = state.transferredCount +
                            result.transferredTeiUids.size,
                        step = TransferStep.SELECT_LEARNERS,
                    )
                }
                _formResetEvent.emit(Unit)
                loadPendingOutgoingTransfers()
                if (result.transferredTeiUids.isNotEmpty()) {
                    emitSuccess(
                        resourceManager.getString(
                            R.string.transfer_success,
                            result.transferredTeiUids.size,
                        ),
                    )
                    _syncEvent.emit(Unit)
                }
                if (result.failures.isNotEmpty()) {
                    emitError(
                        result.failures.joinToString(separator = "\n") { it.message },
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        selectedTab = TransferTab.TRANSFERS,
                        selectedLearnerUids = emptySet(),
                        destinationOrgUnit = null,
                        isTransferFormValid = false,
                        step = TransferStep.SELECT_LEARNERS,
                    )
                }
                _formResetEvent.emit(Unit)
                emitError(
                    error.message ?: resourceManager.getString(R.string.transfer_failed),
                )
            }
        }
    }

    private suspend fun emitSuccess(message: String) {
        _messageEvent.emit(TransferMessage(message, TransferMessageType.SUCCESS))
    }

    private suspend fun emitError(message: String) {
        _messageEvent.emit(TransferMessage(message, TransferMessageType.ERROR))
    }
}
