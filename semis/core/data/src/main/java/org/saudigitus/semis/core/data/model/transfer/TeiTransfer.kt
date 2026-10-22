package org.saudigitus.semis.core.data.model.transfer

import org.saudigitus.semis.core.data.model.person.PersonTag
import java.util.Date

/**
 * A transfer request, seen from either end. The same shape serves the outgoing and the
 * incoming lists because both show the same record; only the actions offered differ.
 *
 * The learner belongs to [originOrgUnit] until the destination approves, so the origin
 * is the organisation unit the request event lives in and is never read from a data
 * element: the origin school data element is optional and requests raised on the app
 * leave it empty.
 *
 * [requestedAt] is when the request was raised, which is what the lists are ordered by
 * and what the relative time shown under the status is measured from.
 *
 * [details] are the columns the web shows for the request: the registration stage values,
 * then the transfer stage values, each one only when its stage displays it in reports.
 */
data class TeiTransfer(
    val eventUid: String,
    val teiUid: String,
    val enrollmentUid: String,
    val recordName: String,
    val identifier: String,
    val photoPath: String? = null,
    val tags: List<PersonTag> = emptyList(),
    val originOrgUnit: String,
    val originSchoolName: String,
    val destinationOrgUnit: String,
    val destinationSchoolName: String,
    val details: List<TransferDetail> = emptyList(),
    val status: TransferStatus,
    val requestedAt: Date,
) {
    /**
     * A learner may only be part of one request at a time. Once the destination has
     * decided, whichever way, the learner is free to be requested again.
     */
    val isPending: Boolean get() = status == TransferStatus.PENDING
}

/** One value of a request as the list shows it. [value] is null when the request left it empty. */
data class TransferDetail(
    val label: String,
    val value: String?,
)
