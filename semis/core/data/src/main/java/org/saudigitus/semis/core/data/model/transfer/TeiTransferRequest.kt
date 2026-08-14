package org.saudigitus.semis.core.data.model.transfer

import java.util.Date

data class TeiTransferRequest(
    val program: String,
    val destinationOrgUnit: String,
    val learners: List<TeiTransferLearner>,
    val effectiveDate: Date,
)
