package org.saudigitus.semis.transfer.model

import androidx.compose.runtime.Immutable
import org.saudigitus.semis.core.data.model.transfer.TransferDecision

/**
 * A decision waiting to be confirmed.
 *
 * Approving hands a learner over to another school and rejecting sends the request back,
 * and neither can be undone from this screen, so both are confirmed first. The learner
 * name is carried so the dialog can name who is being decided about.
 */
@Immutable
data class PendingTransferDecision(
    val eventUid: String,
    val recordName: String,
    val decision: TransferDecision,
)
