package org.saudigitus.semis.transfer.state

import org.saudigitus.semis.core.data.model.OrgUnit
import org.saudigitus.semis.core.data.model.SearchTeiModel
import org.saudigitus.semis.core.data.model.transfer.IncomingTeiTransfer
import org.saudigitus.semis.core.data.model.transfer.OutgoingTeiTransfer
import org.saudigitus.semis.core.designsystem.components.FilterDetailsState
import org.saudigitus.semis.transfer.model.TransferStep
import org.saudigitus.semis.transfer.model.TransferTab
import java.util.Date

data class TransferUiState(
    val isLoading: Boolean = false,
    val isLoadingMetadata: Boolean = false,
    val isLoadingIncoming: Boolean = false,
    val isLoadingOutgoingTransfers: Boolean = false,
    val isSubmitting: Boolean = false,
    val program: String = "",
    val sourceOrgUnit: OrgUnit? = null,
    val originFilterDetails: FilterDetailsState = FilterDetailsState(
        enable = false,
        enableCounter = false,
    ),
    val transferProgramStage: String = "",
    val originSchoolDataElement: String = "",
    val destinationSchoolDataElement: String = "",
    val statusDataElement: String = "",
    val pendingStatusCode: String = "",
    val destinationOrgUnit: OrgUnit? = null,
    val isTransferFormValid: Boolean = false,
    val learners: List<SearchTeiModel> = emptyList(),
    val incomingTransfers: List<IncomingTeiTransfer> = emptyList(),
    val outgoingTransfers: List<OutgoingTeiTransfer> = emptyList(),
    val processingEventUids: Set<String> = emptySet(),
    val selectedIncomingEventUids: Set<String> = emptySet(),
    val selectedTab: TransferTab = TransferTab.TRANSFERS,
    val selectedLearnerUids: Set<String> = emptySet(),
    val effectiveDate: Date = Date(),
    val step: TransferStep = TransferStep.SELECT_LEARNERS,
    val transferredCount: Int = 0,
) {
    val canContinue: Boolean
        get() = when (step) {
            TransferStep.SELECT_LEARNERS -> selectedLearnerUids.isNotEmpty() &&
                !isLoadingMetadata &&
                hasTransferMetadata
            TransferStep.DESTINATION -> !isLoadingMetadata &&
                isTransferFormValid
            TransferStep.REVIEW -> !isSubmitting && destinationOrgUnit != null
        }

    private val hasTransferMetadata: Boolean
        get() = transferProgramStage.isNotBlank() &&
            originSchoolDataElement.isNotBlank() &&
            destinationSchoolDataElement.isNotBlank() &&
            statusDataElement.isNotBlank() &&
            pendingStatusCode.isNotBlank()

    val showTransferActions: Boolean
        get() = selectedTab == TransferTab.TRANSFERS

    /** Whether the incoming tab is showing its own bottom actions. */
    val showIncomingActions: Boolean
        get() = step == TransferStep.SELECT_LEARNERS &&
            selectedTab == TransferTab.INCOMING_STUDENTS &&
            incomingTransfers.isNotEmpty()

    /** Incoming requests picked for a bulk decision. */
    val selectedIncomingTransfers: List<IncomingTeiTransfer>
        get() = incomingTransfers.filter { it.eventUid in selectedIncomingEventUids }

    val hasIncomingSelection: Boolean
        get() = selectedIncomingTransfers.isNotEmpty()

    /**
     * Requests the destination school still has to act on. A transfer that was approved or
     * rejected is no longer pending and drops off the tab.
     */
    val pendingOutgoingTransfers: List<OutgoingTeiTransfer>
        get() = outgoingTransfers.filter { it.isPending }

    private val transferredTeiUids: Set<String>
        get() = outgoingTransfers.mapTo(mutableSetOf()) { it.teiUid }

    /**
     * Learners still available to transfer. Creating a transfer event takes a learner off
     * this list whatever the request went on to become, so none can be sent out twice.
     */
    val outgoingLearners: List<SearchTeiModel>
        get() = transferredTeiUids.let { transferred ->
            if (transferred.isEmpty()) {
                learners
            } else {
                learners.filterNot { it.tei.uid() in transferred }
            }
        }
}
