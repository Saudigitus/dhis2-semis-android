package org.saudigitus.semis.transfer.components.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.saudigitus.semis.core.data.model.transfer.TeiTransfer
import org.saudigitus.semis.core.data.model.transfer.TransferDecision
import org.saudigitus.semis.core.designsystem.components.NoResults
import org.saudigitus.semis.transfer.R
import org.saudigitus.semis.transfer.model.TransferTab
import java.util.Date

/**
 * The body of either tab: the requests, newest first, or a single line saying there are
 * none. A filter that matches nothing says so rather than showing an empty screen.
 */
@Composable
internal fun TransferList(
    transfers: List<TeiTransfer>,
    tab: TransferTab,
    isLoading: Boolean,
    isFiltered: Boolean,
    processingEventUids: Set<String>,
    modifier: Modifier = Modifier,
    emptyMessage: String,
    onDecide: ((String, TransferDecision) -> Unit)? = null,
) {
    val now = Date()

    when {
        isLoading -> Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }

        transfers.isEmpty() -> Box(modifier = modifier.fillMaxWidth()) {
            NoResults(
                message = if (isFiltered) {
                    stringResource(R.string.no_transfers_for_filter)
                } else {
                    emptyMessage
                },
            )
        }

        else -> LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(transfers, key = { it.eventUid }) { transfer ->
                TransferListItem(
                    transfer = transfer,
                    tab = tab,
                    now = now,
                    processing = transfer.eventUid in processingEventUids,
                    onDecide = onDecide?.let { decide ->
                        { decision -> decide(transfer.eventUid, decision) }
                    },
                )
            }
        }
    }
}
