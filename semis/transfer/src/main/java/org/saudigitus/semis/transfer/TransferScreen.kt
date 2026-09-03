package org.saudigitus.semis.transfer

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.saudigitus.semis.core.designsystem.components.AlertDialog
import org.saudigitus.semis.core.designsystem.components.SnackBar
import org.saudigitus.semis.core.designsystem.components.notice.InlineNotice
import org.saudigitus.semis.core.designsystem.templates.RoundedHeaderScaffold
import org.saudigitus.semis.core.designsystem.theme.dark_warning
import org.saudigitus.semis.core.designsystem.theme.light_error
import org.saudigitus.semis.core.designsystem.theme.light_success
import org.saudigitus.semis.core.data.model.transfer.TransferDecision
import org.saudigitus.semis.core.form.ui.state.FormEvent
import org.saudigitus.semis.core.form.ui.state.FormUiState
import org.saudigitus.semis.transfer.components.list.TransferList
import org.saudigitus.semis.transfer.components.list.TransferStatusChips
import org.saudigitus.semis.transfer.components.navigation.TransferBottomBar
import org.saudigitus.semis.transfer.components.navigation.TransferHeader
import org.saudigitus.semis.transfer.components.navigation.TransferProgress
import org.saudigitus.semis.transfer.components.navigation.TransferRequestSheet
import org.saudigitus.semis.transfer.components.navigation.TransferSummarySheet
import org.saudigitus.semis.transfer.components.navigation.continueLabel
import org.saudigitus.semis.transfer.components.request.DestinationStep
import org.saudigitus.semis.transfer.components.request.EntitiesStep
import org.saudigitus.semis.transfer.components.request.ReviewStep
import org.saudigitus.semis.transfer.event.TransferUiEvent
import org.saudigitus.semis.transfer.model.TransferMessageType
import org.saudigitus.semis.transfer.model.TransferStep
import org.saudigitus.semis.transfer.model.TransferTab
import org.saudigitus.semis.transfer.state.TransferUiState
import org.saudigitus.semis.core.designsystem.R as DesignSystemR

@Composable
fun TransferScreen(
    state: TransferUiState,
    formState: FormUiState,
    snackbarHostState: SnackbarHostState,
    snackbarMessageType: TransferMessageType,
    onEvent: (TransferUiEvent) -> Unit,
    onFormEvent: (FormEvent) -> Unit,
    navigateBack: () -> Unit,
) {
    state.pendingDecision?.let { pending ->
        AlertDialog(
            message = stringResource(
                when (pending.decision) {
                    TransferDecision.APPROVE -> R.string.confirm_approve
                    TransferDecision.REJECT -> R.string.confirm_reject
                },
                pending.recordName,
            ),
            onConfirm = { onEvent(TransferUiEvent.ConfirmDecision) },
            onDismissRequest = { onEvent(TransferUiEvent.DismissDecision) },
        )
    }

    RoundedHeaderScaffold(
        header = {
            TransferHeader(
                schoolContext = state.schoolContext,
                selectedTab = state.selectedTab,
                requestStep = state.requestStep,
                onNavigateBack = navigateBack,
                onRefreshIncoming = { onEvent(TransferUiEvent.RefreshIncoming) },
                onRefreshOutgoing = { onEvent(TransferUiEvent.RefreshOutgoing) },
            )
        },
        bottomBar = {
            when (val step = state.requestStep) {
                null -> if (state.selectedTab == TransferTab.OUTGOING) {
                    TransferBottomBar(
                        label = stringResource(R.string.start_transfer),
                        enabled = !state.isLoadingMetadata &&
                            state.transferProgramStage.isNotBlank(),
                        leadingIcon = true,
                        onClick = { onEvent(TransferUiEvent.StartRequest) },
                    )
                }

                else -> TransferBottomBar(
                    label = stringResource(step.continueLabel()),
                    enabled = state.canContinue,
                    isBusy = state.isSubmitting,
                    onClick = { onEvent(TransferUiEvent.Continue) },
                )
            }
        },
        snackbarHost = {
            SnackBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                hostState = snackbarHostState,
                containerColor = if (snackbarMessageType == TransferMessageType.ERROR) {
                    light_error
                } else {
                    light_success
                },
                painter = painterResource(
                    if (snackbarMessageType == TransferMessageType.ERROR) {
                        DesignSystemR.drawable.ic_outline_error_36
                    } else {
                        DesignSystemR.drawable.success_icon
                    },
                ),
            )
        },
    ) {
        when (val step = state.requestStep) {
            null -> {
                TransferSummarySheet(
                    selectedTab = state.selectedTab,
                    onSelectTab = { onEvent(TransferUiEvent.SelectTab(it)) },
                )

                TransferStatusChips(
                    selected = state.statusFilter,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 12.dp),
                    onSelect = { onEvent(TransferUiEvent.SelectStatusFilter(it)) },
                )

                if (state.selectedTab == TransferTab.INCOMING) {
                    InlineNotice(
                        text = stringResource(R.string.incoming_requires_connection),
                        imageVector = Icons.Default.CloudSync,
                        tone = dark_warning,
                        modifier = Modifier.padding(
                            start = 16.dp,
                            end = 16.dp,
                            top = 12.dp,
                        ),
                    )
                }

                TransferList(
                    transfers = state.visibleTransfers,
                    tab = state.selectedTab,
                    isLoading = state.isLoadingTab,
                    isFiltered = state.statusFilter != null,
                    processingEventUids = state.processingEventUids,
                    emptyMessage = stringResource(
                        when (state.selectedTab) {
                            TransferTab.OUTGOING -> R.string.no_outgoing_transfers
                            TransferTab.INCOMING -> R.string.no_incoming_transfers
                        },
                    ),
                    onDecide = if (state.showsDecisionActions) {
                        { eventUid, decision ->
                            onEvent(TransferUiEvent.AskDecision(eventUid, decision))
                        }
                    } else {
                        null
                    },
                )
            }

            else -> {
                TransferRequestSheet {
                    TransferProgress(step = step)
                }

                when (step) {
                    TransferStep.ENTITIES -> EntitiesStep(
                        placement = state.originFilterDetails,
                        records = state.availableRecords,
                        selectedUids = state.selectedRecordUids,
                        onToggle = { onEvent(TransferUiEvent.ToggleRecord(it)) },
                    )

                    TransferStep.DESTINATION -> DestinationStep(
                        state = state,
                        formState = formState,
                        onFormEvent = onFormEvent,
                    )

                    TransferStep.REVIEW -> ReviewStep(
                        state = state,
                        formState = formState,
                    )
                }
            }
        }
    }
}
