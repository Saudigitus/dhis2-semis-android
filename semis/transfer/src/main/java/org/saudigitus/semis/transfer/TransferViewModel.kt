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
import org.saudigitus.semis.core.data.model.transfer.TeiTransferRecord
import org.saudigitus.semis.core.data.model.transfer.TeiTransferRequest
import org.saudigitus.semis.core.data.model.transfer.TransferDecision
import org.saudigitus.semis.core.data.repository.TeiTransferRepository
import org.saudigitus.semis.core.designsystem.components.FilterDetailsState
import org.saudigitus.semis.transfer.event.TransferUiEvent
import org.saudigitus.semis.transfer.model.PendingTransferDecision
import org.saudigitus.semis.transfer.model.TransferMessage
import org.saudigitus.semis.transfer.model.TransferMessageType
import org.saudigitus.semis.transfer.model.TransferStatusFilter
import org.saudigitus.semis.transfer.model.TransferStep
import org.saudigitus.semis.transfer.model.TransferTab
import org.saudigitus.semis.transfer.state.TransferUiState
import java.util.Date
import javax.inject.Inject

/**
 * Drives the two transfer lists and the request form.
 *
 * Raising a request moves nothing: the learner keeps belonging to this school until the
 * destination approves. Deciding is the only operation that hands a learner over, and it
 * is only ever offered on the incoming tab.
 */
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
        records: List<SearchTeiModel>,
        originFilterDetails: FilterDetailsState,
    ) {
        if (initialized) return
        initialized = true

        _uiState.update {
            it.copy(
                program = program,
                sourceOrgUnit = sourceOrgUnit,
                records = records,
                originFilterDetails = originFilterDetails.copy(enable = false),
                isLoadingMetadata = true,
            )
        }
        loadTransferMetadata(program)
        loadOutgoingTransfers()
        refreshIncomingTransfers()
    }

    fun handleUiEvent(event: TransferUiEvent) {
        when (event) {
            is TransferUiEvent.SelectTab -> selectTab(event.tab)
            is TransferUiEvent.SelectStatusFilter -> selectStatusFilter(event.filter)
            TransferUiEvent.RefreshIncoming -> refreshIncomingTransfers()
            is TransferUiEvent.AskDecision -> askDecision(event.eventUid, event.decision)
            TransferUiEvent.ConfirmDecision -> confirmDecision()
            TransferUiEvent.DismissDecision -> dismissDecision()
            TransferUiEvent.StartRequest -> startRequest()
            TransferUiEvent.CancelRequest -> cancelRequest()
            is TransferUiEvent.ToggleRecord -> toggleRecord(event.teiUid)
            TransferUiEvent.Continue -> continueFlow()
            TransferUiEvent.Back -> previousStep()
        }
    }

    /**
     * The destination step is the configured transfer form, so the selected school and
     * the form validity are reported back from the composable that renders it.
     */
    fun updateRequestForm(destinationOrgUnit: OrgUnit?, isValid: Boolean) {
        _uiState.update {
            it.copy(
                destinationOrgUnit = destinationOrgUnit,
                isRequestFormValid = isValid,
            )
        }
    }

    private fun selectTab(tab: TransferTab) {
        _uiState.update { it.copy(selectedTab = tab, statusFilter = null) }
        if (tab == TransferTab.INCOMING) refreshIncomingTransfers()
    }

    private fun selectStatusFilter(filter: TransferStatusFilter) {
        _uiState.update {
            it.copy(statusFilter = filter.takeIf { chosen -> chosen != it.statusFilter })
        }
    }

    private fun loadTransferMetadata(program: String) {
        viewModelScope.launch {
            runCatching { repository.getTransferMetadata(program) }
                .onSuccess { metadata ->
                    _uiState.update {
                        it.copy(
                            isLoadingMetadata = false,
                            transferProgramStage = metadata.programStage,
                            destinationSchoolDataElement = metadata.destinationSchoolDataElement,
                            statusDataElement = metadata.statusDataElement,
                            pendingStatusCode = metadata.pendingStatusCode,
                        )
                    }
                }.onFailure { error ->
                    _uiState.update { it.copy(isLoadingMetadata = false) }
                    emitError(error, R.string.transfer_form_load_failed)
                }
        }
    }

    private fun loadOutgoingTransfers() {
        val current = _uiState.value
        val orgUnit = current.sourceOrgUnit ?: return
        if (current.isLoadingOutgoing) return

        _uiState.update { it.copy(isLoadingOutgoing = true) }
        viewModelScope.launch {
            runCatching { repository.getOutgoingTransfers(current.program, orgUnit.uid) }
                .onSuccess { outgoing ->
                    _uiState.update {
                        it.copy(isLoadingOutgoing = false, outgoingTransfers = outgoing)
                    }
                }.onFailure { error ->
                    _uiState.update { it.copy(isLoadingOutgoing = false) }
                    emitError(error, R.string.outgoing_transfer_load_failed)
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
                    emitError(error, R.string.incoming_transfer_load_failed)
                }
        }
    }

    /**
     * A request addressed to this school lives in the school that raised it, so it is
     * outside what this device downloads for itself. It is fetched first and read from
     * the local store afterwards, which keeps the list usable once it has been seen.
     */
    private fun refreshIncomingTransfers() {
        val current = _uiState.value
        val orgUnit = current.sourceOrgUnit ?: return
        if (current.isDownloadingIncoming) return

        _uiState.update { it.copy(isDownloadingIncoming = true) }
        viewModelScope.launch {
            runCatching {
                repository.downloadIncomingTransfers(current.program, orgUnit.uid)
            }.onFailure { error ->
                emitError(error, R.string.incoming_transfer_load_failed)
            }
            _uiState.update { it.copy(isDownloadingIncoming = false) }
            loadIncomingTransfers()
        }
    }

    /**
     * Holds the decision until it is confirmed. Neither approving nor rejecting can be
     * undone from this screen, so neither is applied on a single tap.
     */
    private fun askDecision(eventUid: String, decision: TransferDecision) {
        val transfer = _uiState.value.incomingTransfers
            .firstOrNull { it.eventUid == eventUid }
            ?: return

        _uiState.update {
            it.copy(
                pendingDecision = PendingTransferDecision(
                    eventUid = eventUid,
                    recordName = transfer.recordName,
                    decision = decision,
                ),
            )
        }
    }

    private fun dismissDecision() {
        _uiState.update { it.copy(pendingDecision = null) }
    }

    private fun confirmDecision() {
        val pending = _uiState.value.pendingDecision ?: return
        _uiState.update { it.copy(pendingDecision = null) }
        decide(pending.eventUid, pending.decision)
    }

    private fun decide(eventUid: String, decision: TransferDecision) {
        val current = _uiState.value
        val orgUnit = current.sourceOrgUnit ?: return
        if (eventUid in current.processingEventUids) return

        _uiState.update { it.copy(processingEventUids = it.processingEventUids + eventUid) }

        viewModelScope.launch {
            runCatching {
                repository.decideIncomingTransfer(
                    program = current.program,
                    currentOrgUnit = orgUnit.uid,
                    eventUid = eventUid,
                    decision = decision,
                )
            }.onSuccess {
                _uiState.update {
                    it.copy(processingEventUids = it.processingEventUids - eventUid)
                }
                emitSuccess(resourceManager.getString(decision.successMessage()))
                _syncEvent.emit(Unit)
                loadIncomingTransfers()
            }.onFailure { error ->
                _uiState.update {
                    it.copy(processingEventUids = it.processingEventUids - eventUid)
                }
                emitError(error, decision.failureMessage())
            }
        }
    }

    private fun startRequest() {
        _uiState.update {
            it.copy(
                requestStep = TransferStep.ENTITIES,
                selectedRecordUids = emptySet(),
                destinationOrgUnit = null,
                isRequestFormValid = false,
                effectiveDate = Date(),
            )
        }
    }

    private fun cancelRequest() {
        _uiState.update {
            it.copy(
                requestStep = null,
                selectedRecordUids = emptySet(),
                destinationOrgUnit = null,
                isRequestFormValid = false,
            )
        }
        viewModelScope.launch { _formResetEvent.emit(Unit) }
    }

    private fun toggleRecord(teiUid: String) {
        _uiState.update { current ->
            val selected = current.selectedRecordUids.toMutableSet()
            if (!selected.add(teiUid)) selected.remove(teiUid)
            current.copy(selectedRecordUids = selected)
        }
    }

    private fun continueFlow() {
        val current = _uiState.value
        if (!current.canContinue) return

        when (current.requestStep) {
            TransferStep.ENTITIES -> _uiState.update {
                it.copy(requestStep = TransferStep.DESTINATION)
            }

            TransferStep.DESTINATION -> _uiState.update {
                it.copy(requestStep = TransferStep.REVIEW)
            }

            TransferStep.REVIEW -> submitRequest()
            null -> Unit
        }
    }

    /** Backing out of the first step closes the request and returns to the lists. */
    private fun previousStep() {
        val current = _uiState.value
        when (current.requestStep) {
            TransferStep.ENTITIES, null -> cancelRequest()
            TransferStep.DESTINATION -> _uiState.update {
                it.copy(requestStep = TransferStep.ENTITIES)
            }

            TransferStep.REVIEW -> _uiState.update {
                it.copy(requestStep = TransferStep.DESTINATION)
            }
        }
    }

    private fun submitRequest() {
        val current = _uiState.value
        val destination = current.destinationOrgUnit ?: return
        val records = current.selectedRecords.mapNotNull { record ->
            record.selectedEnrollment?.uid()?.let { enrollmentUid ->
                TeiTransferRecord(
                    teiUid = record.tei.uid(),
                    enrollmentUid = enrollmentUid,
                )
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            runCatching {
                repository.requestTransfer(
                    TeiTransferRequest(
                        program = current.program,
                        destinationOrgUnit = destination.uid,
                        records = records,
                        effectiveDate = current.effectiveDate,
                    ),
                )
            }.onSuccess { result ->
                closeRequest()
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
                closeRequest()
                emitError(error, R.string.transfer_failed)
            }
        }
    }

    private suspend fun closeRequest() {
        _uiState.update {
            it.copy(
                isSubmitting = false,
                requestStep = null,
                selectedTab = TransferTab.OUTGOING,
                selectedRecordUids = emptySet(),
                destinationOrgUnit = null,
                isRequestFormValid = false,
            )
        }
        _formResetEvent.emit(Unit)
    }

    private suspend fun emitSuccess(message: String) {
        _messageEvent.emit(TransferMessage(message, TransferMessageType.SUCCESS))
    }

    private suspend fun emitError(message: String) {
        _uiState.update { it.copy(errorMessage = message) }
        _messageEvent.emit(TransferMessage(message, TransferMessageType.ERROR))
    }

    private suspend fun emitError(error: Throwable, fallback: Int) {
        emitError(error.message ?: resourceManager.getString(fallback))
    }
}

private fun TransferDecision.successMessage() = when (this) {
    TransferDecision.APPROVE -> R.string.incoming_transfer_approved
    TransferDecision.REJECT -> R.string.incoming_transfer_rejected
}

private fun TransferDecision.failureMessage() = when (this) {
    TransferDecision.APPROVE -> R.string.incoming_transfer_approval_failed
    TransferDecision.REJECT -> R.string.incoming_transfer_rejection_failed
}
