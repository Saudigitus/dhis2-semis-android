package org.saudigitus.semis.transfer.event

import org.saudigitus.semis.core.data.model.transfer.TransferDecision
import org.saudigitus.semis.transfer.model.TransferStatusFilter
import org.saudigitus.semis.transfer.model.TransferTab

sealed interface TransferUiEvent {
    data class SelectTab(val tab: TransferTab) : TransferUiEvent

    /** Selecting the chip already applied clears the filter and shows every request. */
    data class SelectStatusFilter(val filter: TransferStatusFilter) : TransferUiEvent

    data object RefreshIncoming : TransferUiEvent

    /**
     * Asks the server for news about the requests this school raised: their statuses,
     * and the departure of any learner whose transfer was approved.
     */
    data object RefreshOutgoing : TransferUiEvent

    /** Asks for the decision to be confirmed rather than applying it. */
    data class AskDecision(
        val eventUid: String,
        val decision: TransferDecision,
    ) : TransferUiEvent

    data object ConfirmDecision : TransferUiEvent

    data object DismissDecision : TransferUiEvent

    data object StartRequest : TransferUiEvent

    data object CancelRequest : TransferUiEvent

    data class ToggleRecord(val teiUid: String) : TransferUiEvent

    data object Continue : TransferUiEvent

    data object Back : TransferUiEvent
}
