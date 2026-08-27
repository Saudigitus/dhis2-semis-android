package org.saudigitus.semis.core.data.model.transfer

import java.util.Date

/**
 * A transfer request, seen from either end. The same shape serves the outgoing and the
 * incoming lists because both show the same record; only the actions offered differ.
 *
 * The learner belongs to [originOrgUnit] until the destination approves, so the origin
 * is the organisation unit the request event lives in and is never read from a data
 * element: the transfer program stage holds a single organisation unit data element and
 * it carries the destination.
 *
 * [requestedAt] is when the request was raised, which is what the lists are ordered by
 * and what the relative time shown under the status is measured from.
 *
 * [grade] and [reason] are what a school needs to decide without opening the request:
 * which class the learner is in and why they are being sent. Both are blank when the
 * configuration does not name them or the request left them empty.
 */
data class TeiTransfer(
    val eventUid: String,
    val teiUid: String,
    val enrollmentUid: String,
    val recordName: String,
    val firstAttributeValue: String,
    val originOrgUnit: String,
    val originSchoolName: String,
    val destinationOrgUnit: String,
    val destinationSchoolName: String,
    val grade: String,
    val reason: String,
    val status: TransferStatus,
    val requestedAt: Date,
) {
    /**
     * A learner may only be part of one request at a time. Once the destination has
     * decided, whichever way, the learner is free to be requested again.
     */
    val isPending: Boolean get() = status == TransferStatus.PENDING
}
