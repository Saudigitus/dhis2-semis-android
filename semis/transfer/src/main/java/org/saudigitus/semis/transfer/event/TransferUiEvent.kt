package org.saudigitus.semis.transfer.event

import org.saudigitus.semis.transfer.model.TransferTab

sealed interface TransferUiEvent {
    data class SelectTab(val tab: TransferTab) : TransferUiEvent
    data class ToggleLearner(val teiUid: String) : TransferUiEvent
    data class ApproveIncoming(val eventUid: String) : TransferUiEvent
    data object Continue : TransferUiEvent
    data object Back : TransferUiEvent
}
