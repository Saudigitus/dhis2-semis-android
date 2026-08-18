package org.saudigitus.semis.transfer.components.outgoing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.saudigitus.semis.transfer.R
import org.saudigitus.semis.transfer.components.common.StepIntroduction
import org.saudigitus.semis.transfer.state.TransferUiState

@Composable
internal fun PendingOutgoingStudents(state: TransferUiState) {
    when {
        state.isLoadingPendingOutgoing -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) { CircularProgressIndicator() }

        state.pendingOutgoingTransfers.isEmpty() -> EmptyPendingOutgoingStudents()

        else -> Column(modifier = Modifier.fillMaxSize()) {
            StepIntroduction(
                title = stringResource(R.string.pending_outgoing_students),
                description = stringResource(
                    R.string.pending_outgoing_description,
                    state.pendingOutgoingTransfers.size,
                ),
                badge = state.pendingOutgoingTransfers.size.toString(),
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                items(state.pendingOutgoingTransfers, key = { it.eventUid }) { transfer ->
                    PendingOutgoingStudentCard(transfer = transfer)
                }
            }
        }
    }
}
