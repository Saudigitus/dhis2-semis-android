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
    val isLoadingPendingOutgoing: Boolean = false,
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
    val pendingOutgoingTransfers: List<OutgoingTeiTransfer> = emptyList(),
    val approvingEventUids: Set<String> = emptySet(),
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

    private val pendingOutgoingTeiUids: Set<String>
        get() = pendingOutgoingTransfers.mapTo(mutableSetOf()) { it.teiUid }

    /**
     * Learners still available to transfer. A learner whose transfer is awaiting approval
     * is listed under the pending tab instead, so it cannot be sent out twice.
     */
    val outgoingLearners: List<SearchTeiModel>
        get() = pendingOutgoingTeiUids.let { pending ->
            if (pending.isEmpty()) learners else learners.filterNot { it.tei.uid() in pending }
        }
}
