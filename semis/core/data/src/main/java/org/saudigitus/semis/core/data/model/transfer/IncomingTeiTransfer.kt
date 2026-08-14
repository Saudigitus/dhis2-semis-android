package org.saudigitus.semis.core.data.model.transfer

import java.util.Date

data class IncomingTeiTransfer(
    val eventUid: String,
    val teiUid: String,
    val enrollmentUid: String,
    val learnerName: String,
    val firstAttributeValue: String,
    val originOrgUnit: String,
    val originSchoolName: String,
    val destinationOrgUnit: String,
    val effectiveDate: Date,
)
