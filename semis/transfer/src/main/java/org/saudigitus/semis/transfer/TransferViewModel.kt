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
import org.saudigitus.semis.core.data.model.transfer.TransferDecision
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
        loadOutgoingTransfers()
    }

    fun handleEvent(event: TransferUiEvent) {
        when (event) {
            is TransferUiEvent.SelectTab -> selectTab(event.tab)
            is TransferUiEvent.ToggleLearner -> toggleLearner(event.teiUid)
            is TransferUiEvent.DecideIncoming -> decideIncoming(event.eventUid, event.decision)
            is TransferUiEvent.ToggleIncomingSelection -> toggleIncomingSelection(event.eventUid)
            is TransferUiEvent.DecideSelectedIncoming -> decideSelectedIncoming(event.decision)
            TransferUiEvent.ApproveAllIncoming -> approveAllIncoming()
            TransferUiEvent.ClearIncomingSelection -> clearIncomingSelection()
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

    private fun loadOutgoingTransfers() {
        val current = _uiState.value
        val orgUnit = current.sourceOrgUnit ?: return
        if (current.isLoadingOutgoingTransfers) return

        _uiState.update { it.copy(isLoadingOutgoingTransfers = true) }
        viewModelScope.launch {
            runCatching {
                repository.getOutgoingTransfers(current.program, orgUnit.uid)
            }.onSuccess { outgoing ->
                _uiState.update {
                    it.copy(isLoadingOutgoingTransfers = false, outgoingTransfers = outgoing)
                }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoadingOutgoingTransfers = false) }
                emitError(
                    error.message
                        ?: resourceManager.getString(R.string.pending_outgoing_load_failed),
                )
            }
        }
    }

    private fun toggleIncomingSelection(eventUid: String) {
        _uiState.update { current ->
            val selected = current.selectedIncomingEventUids.toMutableSet()
            if (!selected.add(eventUid)) selected.remove(eventUid)
            current.copy(selectedIncomingEventUids = selected)
        }
    }

    private fun clearIncomingSelection() {
        _uiState.update { it.copy(selectedIncomingEventUids = emptySet()) }
    }

    private fun approveAllIncoming() {
        decideIncoming(
            eventUids = _uiState.value.incomingTransfers.map { it.eventUid },
            decision = TransferDecision.APPROVE,
        )
    }

    private fun decideSelectedIncoming(decision: TransferDecision) {
        decideIncoming(
            eventUids = _uiState.value.selectedIncomingTransfers.map { it.eventUid },
            decision = decision,
        )
    }

    private fun decideIncoming(eventUid: String, decision: TransferDecision) {
        decideIncoming(eventUids = listOf(eventUid), decision = decision)
    }

    /**
     * Applies [decision] to each request in turn so one failure leaves the others decided,
     * and reports how many went through.
     */
    private fun decideIncoming(eventUids: List<String>, decision: TransferDecision) {
        val current = _uiState.value
        val orgUnit = current.sourceOrgUnit ?: return
        val pending = eventUids.filterNot { it in current.processingEventUids }
        if (pending.isEmpty()) return

        _uiState.update { it.copy(processingEventUids = it.processingEventUids + pending) }

        viewModelScope.launch {
            val decided = mutableListOf<String>()
            val failures = mutableListOf<String>()

            pending.forEach { eventUid ->
                runCatching {
                    repository.decideIncomingTransfer(
                        program = current.program,
                        currentOrgUnit = orgUnit.uid,
                        eventUid = eventUid,
                        decision = decision,
                    )
                }.onSuccess {
                    decided += eventUid
                }.onFailure { error ->
                    failures += error.message
                        ?: resourceManager.getString(decision.failureMessage())
                }
            }

            _uiState.update { state ->
                state.copy(
                    processingEventUids = state.processingEventUids - pending.toSet(),
                    selectedIncomingEventUids = state.selectedIncomingEventUids - decided.toSet(),
                    incomingTransfers = state.incomingTransfers.filterNot {
                        it.eventUid in decided
                    },
                )
            }

            if (decided.isNotEmpty()) {
                emitSuccess(
                    resourceManager.getString(decision.successMessage(), decided.size),
                )
                _syncEvent.emit(Unit)
            }
            if (failures.isNotEmpty()) {
                emitError(failures.distinct().joinToString(separator = "\n"))
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
                loadOutgoingTransfers()
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

private fun TransferDecision.successMessage() = when (this) {
    TransferDecision.APPROVE -> R.string.incoming_transfers_approved
    TransferDecision.REJECT -> R.string.incoming_transfers_rejected
}

private fun TransferDecision.failureMessage() = when (this) {
    TransferDecision.APPROVE -> R.string.incoming_transfer_approval_failed
    TransferDecision.REJECT -> R.string.incoming_transfer_rejection_failed
}
