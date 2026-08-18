package org.saudigitus.semis.transfer

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.saudigitus.semis.core.designsystem.components.SnackBar
import org.saudigitus.semis.core.designsystem.components.ToolbarActionState
import org.saudigitus.semis.core.designsystem.components.model.ToolbarHeaders
import org.saudigitus.semis.core.designsystem.templates.TopAppBarScaffold
import org.saudigitus.semis.core.designsystem.theme.light_error
import org.saudigitus.semis.core.designsystem.theme.light_success
import org.saudigitus.semis.core.form.ui.state.FormEvent
import org.saudigitus.semis.core.form.ui.state.FormUiState
import org.saudigitus.semis.transfer.components.destination.DestinationStep
import org.saudigitus.semis.transfer.components.incoming.IncomingActionsBar
import org.saudigitus.semis.transfer.components.navigation.TransferBottomBar
import org.saudigitus.semis.transfer.components.navigation.TransferLanding
import org.saudigitus.semis.transfer.components.navigation.TransferStepContainer
import org.saudigitus.semis.transfer.components.review.ReviewStep
import org.saudigitus.semis.transfer.event.TransferUiEvent
import org.saudigitus.semis.transfer.model.TransferMessageType
import org.saudigitus.semis.transfer.model.TransferStep
import org.saudigitus.semis.transfer.model.TransferTab
import org.saudigitus.semis.transfer.state.TransferUiState
import org.saudigitus.semis.core.designsystem.R as DesignSystemR

private val TransferCanvas = Color(0xFFF5F7FB)

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
    TopAppBarScaffold(
        toolbarHeaders = ToolbarHeaders(
            title = stringResource(R.string.transfer),
            subtitle = stringResource(
                if (state.step == TransferStep.SELECT_LEARNERS) {
                    when (state.selectedTab) {
                        TransferTab.TRANSFERS -> state.step.subtitleResource()
                        TransferTab.INCOMING_STUDENTS -> R.string.incoming_students_subtitle
                        TransferTab.PENDING_OUTGOING -> R.string.pending_outgoing_subtitle
                    }
                } else {
                    state.step.subtitleResource()
                }
            ),
        ),
        toolbarActionState = ToolbarActionState(
            syncVisibility = false,
            filterVisibility = false,
        ),
        navigationAction = navigateBack,
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
                    }
                ),
            )
        },
        bottomBar = {
            when {
                state.showTransferActions -> TransferBottomBar(
                    state = state,
                    onContinue = { onEvent(TransferUiEvent.Continue) },
                )

                state.showIncomingActions -> IncomingActionsBar(
                    selectedCount = state.selectedIncomingTransfers.size,
                    enabled = state.processingEventUids.isEmpty(),
                    onApproveAll = { onEvent(TransferUiEvent.ApproveAllIncoming) },
                    onDecideSelected = {
                        onEvent(TransferUiEvent.DecideSelectedIncoming(it))
                    },
                    onClearSelection = {
                        onEvent(TransferUiEvent.ClearIncomingSelection)
                    },
                )
            }
        },
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = if (MaterialTheme.colorScheme.background == Color.White) {
                TransferCanvas
            } else {
                MaterialTheme.colorScheme.background
            },
        ) {
            when (state.step) {
                TransferStep.SELECT_LEARNERS -> TransferLanding(state, onEvent)
                TransferStep.DESTINATION -> TransferStepContainer(state.step) {
                    DestinationStep(state, formState, onFormEvent)
                }
                TransferStep.REVIEW -> TransferStepContainer(state.step) {
                    ReviewStep(state, formState)
                }
            }
        }
    }
}

private fun TransferStep.subtitleResource(): Int = when (this) {
    TransferStep.SELECT_LEARNERS -> R.string.step_select
    TransferStep.DESTINATION -> R.string.step_destination
    TransferStep.REVIEW -> R.string.step_review
}
