package org.saudigitus.semis.core.data.repository

import org.saudigitus.semis.core.data.model.transfer.TeiTransferMetadata
import org.saudigitus.semis.core.data.model.transfer.TransferDecision
import org.saudigitus.semis.core.data.model.transfer.TeiTransferRequest
import org.saudigitus.semis.core.data.model.transfer.TeiTransferResult
import org.saudigitus.semis.core.data.model.transfer.IncomingTeiTransfer
import org.saudigitus.semis.core.data.model.transfer.OutgoingTeiTransfer

interface TeiTransferRepository {
    suspend fun getTransferMetadata(program: String): TeiTransferMetadata

    suspend fun transfer(request: TeiTransferRequest): TeiTransferResult

    suspend fun getIncomingTransfers(
        program: String,
        currentOrgUnit: String,
    ): List<IncomingTeiTransfer>

    /**
     * Every transfer sent from [currentOrgUnit], whatever its status. A learner holding
     * one is no longer available to transfer; those still awaiting approval are flagged
     * through [OutgoingTeiTransfer.isPending].
     */
    suspend fun getOutgoingTransfers(
        program: String,
        currentOrgUnit: String,
    ): List<OutgoingTeiTransfer>

    /**
     * Records what this school decided about an incoming request. Approving keeps the
     * learner here, rejecting hands them back to the school that sent them.
     */
    suspend fun decideIncomingTransfer(
        program: String,
        currentOrgUnit: String,
        eventUid: String,
        decision: TransferDecision,
    )
}
