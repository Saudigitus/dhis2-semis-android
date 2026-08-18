package org.saudigitus.semis.core.data.repository

import org.saudigitus.semis.core.data.model.transfer.TeiTransferMetadata
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
     * Transfers sent from [currentOrgUnit] that are still awaiting approval at their
     * destination school.
     */
    suspend fun getPendingOutgoingTransfers(
        program: String,
        currentOrgUnit: String,
    ): List<OutgoingTeiTransfer>

    suspend fun approveIncomingTransfer(
        program: String,
        currentOrgUnit: String,
        eventUid: String,
    )
}
