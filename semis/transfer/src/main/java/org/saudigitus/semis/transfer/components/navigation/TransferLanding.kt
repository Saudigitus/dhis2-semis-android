package org.saudigitus.semis.transfer.components.navigation

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.saudigitus.semis.transfer.components.incoming.IncomingStudents
import org.saudigitus.semis.transfer.components.learner.LearnerList
import org.saudigitus.semis.transfer.components.outgoing.PendingOutgoingStudents
import org.saudigitus.semis.transfer.event.TransferUiEvent
import org.saudigitus.semis.transfer.model.TransferTab
import org.saudigitus.semis.transfer.state.TransferUiState

@Composable
internal fun TransferLanding(
    state: TransferUiState,
    onEvent: (TransferUiEvent) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TransferTabs(
            selectedTab = state.selectedTab,
            incomingCount = state.incomingTransfers.size,
            pendingOutgoingCount = state.pendingOutgoingTransfers.size,
        ) { onEvent(TransferUiEvent.SelectTab(it)) }
        Crossfade(
            modifier = Modifier.fillMaxSize(),
            targetState = state.selectedTab,
            animationSpec = tween(durationMillis = 150),
            label = "transfer_tab_content",
        ) { tab ->
            when (tab) {
                TransferTab.TRANSFERS -> Column(modifier = Modifier.fillMaxSize()) {
                    TransferProgress(state.step)
                    LearnerList(state, onEvent)
                }
                TransferTab.INCOMING_STUDENTS -> IncomingStudents(state, onEvent)
                TransferTab.PENDING_OUTGOING -> PendingOutgoingStudents(state)
            }
        }
    }
}
