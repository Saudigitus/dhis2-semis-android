package org.saudigitus.semis.core.data.model.transfer

import java.util.Date

/**
 * A transfer this school sent out that the destination school has not approved yet.
 */
data class OutgoingTeiTransfer(
    val eventUid: String,
    val teiUid: String,
    val enrollmentUid: String,
    val learnerName: String,
    val firstAttributeValue: String,
    val destinationOrgUnit: String,
    val destinationSchoolName: String,
    val effectiveDate: Date,
)
