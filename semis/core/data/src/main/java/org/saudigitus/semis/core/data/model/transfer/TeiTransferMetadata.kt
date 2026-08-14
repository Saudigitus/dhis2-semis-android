package org.saudigitus.semis.core.data.model.transfer

data class TeiTransferMetadata(
    val programStage: String,
    val originSchoolDataElement: String,
    val destinationSchoolDataElement: String,
    val statusDataElement: String,
    val pendingStatusCode: String,
)
