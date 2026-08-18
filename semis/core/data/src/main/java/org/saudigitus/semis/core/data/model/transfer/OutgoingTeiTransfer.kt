package org.saudigitus.semis.core.data.model.transfer

import java.util.Date

/**
 * A transfer this school sent out.
 *
 * [isPending] tells whether the destination school still has to act on it. Approving a
 * transfer only completes the event and leaves its status value untouched, so a request
 * counts as pending only while the event is active and still carries the pending code.
 */
data class OutgoingTeiTransfer(
    val eventUid: String,
    val teiUid: String,
    val enrollmentUid: String,
    val learnerName: String,
    val firstAttributeValue: String,
    val destinationOrgUnit: String,
    val destinationSchoolName: String,
    val statusCode: String,
    val isPending: Boolean,
    val effectiveDate: Date,
)
