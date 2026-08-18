package org.saudigitus.semis.transfer.event

import org.saudigitus.semis.core.data.model.transfer.TransferDecision
import org.saudigitus.semis.transfer.model.TransferTab

sealed interface TransferUiEvent {
    data class SelectTab(val tab: TransferTab) : TransferUiEvent
    data class ToggleLearner(val teiUid: String) : TransferUiEvent

    /** Approves or rejects a single incoming request. */
    data class DecideIncoming(
        val eventUid: String,
        val decision: TransferDecision,
    ) : TransferUiEvent

    /** Adds or removes an incoming request from the bulk selection. */
    data class ToggleIncomingSelection(val eventUid: String) : TransferUiEvent

    /** Applies [decision] to every selected incoming request. */
    data class DecideSelectedIncoming(val decision: TransferDecision) : TransferUiEvent

    /** Approves every incoming request currently listed. */
    data object ApproveAllIncoming : TransferUiEvent

    data object ClearIncomingSelection : TransferUiEvent
    data object Continue : TransferUiEvent
    data object Back : TransferUiEvent
}
