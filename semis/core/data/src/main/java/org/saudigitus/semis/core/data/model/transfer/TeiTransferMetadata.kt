package org.saudigitus.semis.core.data.model.transfer

/**
 * The pieces of the transfer configuration the screen needs to build a request.
 *
 * There is no origin school data element: the origin is the organisation unit the
 * request event belongs to.
 */
data class TeiTransferMetadata(
    val programStage: String,
    val destinationSchoolDataElement: String,
    val statusDataElement: String,
    val pendingStatusCode: String,
)
