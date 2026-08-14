package org.saudigitus.semis.core.data.repository

import org.saudigitus.semis.core.data.model.transfer.TeiTransferMetadata
import org.saudigitus.semis.core.data.model.transfer.TeiTransferRequest
import org.saudigitus.semis.core.data.model.transfer.TeiTransferResult
import org.saudigitus.semis.core.data.model.transfer.IncomingTeiTransfer

interface TeiTransferRepository {
    suspend fun getTransferMetadata(program: String): TeiTransferMetadata

    suspend fun transfer(request: TeiTransferRequest): TeiTransferResult

    suspend fun getIncomingTransfers(
        program: String,
        currentOrgUnit: String,
    ): List<IncomingTeiTransfer>

    suspend fun approveIncomingTransfer(
        program: String,
        currentOrgUnit: String,
        eventUid: String,
    )
}
