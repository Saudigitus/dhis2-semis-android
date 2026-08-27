package org.saudigitus.semis.core.data.repository

import org.saudigitus.semis.core.data.model.transfer.TeiTransfer
import org.saudigitus.semis.core.data.model.transfer.TeiTransferMetadata
import org.saudigitus.semis.core.data.model.transfer.TeiTransferRequest
import org.saudigitus.semis.core.data.model.transfer.TeiTransferResult
import org.saudigitus.semis.core.data.model.transfer.TransferDecision

/**
 * The transfer of a learner between two schools.
 *
 * A transfer is a request the receiving school has to approve. Raising it moves
 * nothing: the learner keeps belonging to the origin school, keeps being marked there
 * and keeps counting in its reports until the destination decides. Approving is the
 * only operation that moves the learner.
 */
interface TeiTransferRepository {
    suspend fun getTransferMetadata(program: String): TeiTransferMetadata

    /**
     * Raises one request per learner on the origin school, carrying the destination and
     * the pending status. Failures are reported per learner so that one rejected
     * learner does not discard the rest of the batch.
     */
    suspend fun requestTransfer(request: TeiTransferRequest): TeiTransferResult

    /**
     * Requests raised by [currentOrgUnit], whatever their status, newest first. The
     * origin is the organisation unit the request lives in, so this is simply what this
     * school asked for.
     */
    suspend fun getOutgoingTransfers(
        program: String,
        currentOrgUnit: String,
    ): List<TeiTransfer>

    /**
     * Requests addressed to [currentOrgUnit], whatever their status, newest first.
     *
     * These live in the origin school, so they are only present locally once
     * [downloadIncomingTransfers] has brought them in.
     */
    suspend fun getIncomingTransfers(
        program: String,
        currentOrgUnit: String,
    ): List<TeiTransfer>

    /**
     * Fetches the learners other schools addressed to [currentOrgUnit] and stores them
     * locally, so the incoming list can be read offline afterwards.
     *
     * A pending request belongs to the origin school and its learner is still owned
     * there, so it is outside what this device downloads for its own school. Requires
     * connectivity, and returns how many learners were brought in.
     */
    suspend fun downloadIncomingTransfers(
        program: String,
        currentOrgUnit: String,
    ): Int

    /**
     * Records what this school decided about a request addressed to it.
     *
     * Approving hands the learner over: the ownership, the enrollment and the history
     * the configuration lists all move to this school. Rejecting only records the
     * decision, because nothing had moved while the request was pending. Either way the
     * request itself stays in the origin school, where it was raised.
     */
    suspend fun decideIncomingTransfer(
        program: String,
        currentOrgUnit: String,
        eventUid: String,
        decision: TransferDecision,
    )
}
