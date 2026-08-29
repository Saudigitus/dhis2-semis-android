package org.saudigitus.semis.transfer.state

import androidx.compose.runtime.Immutable
import org.saudigitus.semis.core.data.model.OrgUnit
import org.saudigitus.semis.core.data.model.SearchTeiModel
import org.saudigitus.semis.core.data.model.transfer.TeiTransfer
import org.saudigitus.semis.core.designsystem.components.FilterDetailsState
import org.saudigitus.semis.transfer.model.PendingTransferDecision
import org.saudigitus.semis.transfer.model.TransferStatusFilter
import org.saudigitus.semis.transfer.model.TransferStep
import org.saudigitus.semis.transfer.model.TransferTab
import org.saudigitus.semis.transfer.model.filterBy
import java.util.Date

@Immutable
data class TransferUiState(
    val isLoadingMetadata: Boolean = false,
    val isLoadingOutgoing: Boolean = false,
    val isLoadingIncoming: Boolean = false,
    val isDownloadingIncoming: Boolean = false,
    val isSubmitting: Boolean = false,
    val program: String = "",
    val sourceOrgUnit: OrgUnit? = null,
    val originFilterDetails: FilterDetailsState = FilterDetailsState(
        enable = false,
        enableCounter = false,
    ),
    val transferProgramStage: String = "",
    val destinationSchoolDataElement: String = "",
    val statusDataElement: String = "",
    val pendingStatusCode: String = "",
    val records: List<SearchTeiModel> = emptyList(),
    val outgoingTransfers: List<TeiTransfer> = emptyList(),
    val incomingTransfers: List<TeiTransfer> = emptyList(),
    val statusFilter: TransferStatusFilter? = null,
    val selectedTab: TransferTab = TransferTab.OUTGOING,
    val processingEventUids: Set<String> = emptySet(),
    val pendingDecision: PendingTransferDecision? = null,
    val requestStep: TransferStep? = null,
    val selectedRecordUids: Set<String> = emptySet(),
    val destinationOrgUnit: OrgUnit? = null,
    val isRequestFormValid: Boolean = false,
    val requestValues: List<Pair<String, String>> = emptyList(),
    val effectiveDate: Date = Date(),
    val errorMessage: String? = null,
) {

    /** The request form takes over the screen while a step is active. */
    val isRequesting: Boolean get() = requestStep != null

    /**
     * What the lists are being read for, shown on the second row of the bar. Grade and
     * class are left out: a transfer moves the learner out of the school altogether, so
     * neither narrows the lists nor changes what a school decides.
     */
    val schoolContext: String
        get() = listOfNotNull(
            sourceOrgUnit?.displayName,
            originFilterDetails.academicYear,
        ).filter { it.isNotBlank() }.joinToString(separator = " · ")

    /** The list behind the selected tab, before the status chips narrow it. */
    val tabTransfers: List<TeiTransfer>
        get() = when (selectedTab) {
            TransferTab.OUTGOING -> outgoingTransfers
            TransferTab.INCOMING -> incomingTransfers
        }

    val visibleTransfers: List<TeiTransfer> get() = tabTransfers.filterBy(statusFilter)

    val isLoadingTab: Boolean
        get() = when (selectedTab) {
            TransferTab.OUTGOING -> isLoadingOutgoing
            TransferTab.INCOMING -> isLoadingIncoming || isDownloadingIncoming
        }

    /**
     * Only the destination school decides, so the actions belong to that tab alone.
     */
    val showsDecisionActions: Boolean get() = selectedTab == TransferTab.INCOMING

    /** Records already awaiting a decision cannot be part of a second request. */
    private val recordsAwaitingDecision: Set<String>
        get() = outgoingTransfers
            .filter { it.isPending }
            .mapTo(mutableSetOf()) { it.teiUid }

    val availableRecords: List<SearchTeiModel>
        get() = recordsAwaitingDecision.let { awaiting ->
            records.filterNot { it.tei.uid() in awaiting }
        }

    val selectedRecords: List<SearchTeiModel>
        get() = records.filter { it.tei.uid() in selectedRecordUids }

    val canContinue: Boolean
        get() = when (requestStep) {
            TransferStep.ENTITIES -> selectedRecordUids.isNotEmpty()
            TransferStep.DESTINATION -> isRequestFormValid && destinationOrgUnit != null
            TransferStep.REVIEW -> !isSubmitting && destinationOrgUnit != null
            null -> false
        }
}
