package org.saudigitus.semis.core.data.model.transfer

import java.util.Date

/**
 * What a school is asking for.
 *
 * [values] carries the rest of the configured transfer form, the reason among them.
 * The destination and the status are set from the request itself and are not expected
 * here, so that a form field can never contradict what was chosen.
 */
data class TeiTransferRequest(
    val program: String,
    val destinationOrgUnit: String,
    val records: List<TeiTransferRecord>,
    val effectiveDate: Date,
    val values: List<Pair<String, String>> = emptyList(),
)
