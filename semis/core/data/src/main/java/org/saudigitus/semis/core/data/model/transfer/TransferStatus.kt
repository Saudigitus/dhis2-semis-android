package org.saudigitus.semis.core.data.model.transfer

import org.saudigitus.semis.core.data.model.app_config.Transfer
import org.saudigitus.semis.core.data.model.app_config.approvedStatusCode
import org.saudigitus.semis.core.data.model.app_config.pendingStatusCode
import org.saudigitus.semis.core.data.model.app_config.rejectedStatusCode

/**
 * Where a transfer request stands. The codes written on the event are configuration
 * driven, so the raw value is normalised into this closed set before it reaches the UI,
 * which decides colour and wording from it.
 *
 * [UNKNOWN] covers a request carrying a code no longer present in the configuration. It
 * is kept visible rather than dropped, so a misconfiguration never hides a learner who
 * is midway through a transfer.
 */
enum class TransferStatus {
    PENDING,
    APPROVED,
    REJECTED,
    UNKNOWN,
}

/**
 * Reads [statusCode] against the configured status options. Matching is done on the
 * codes themselves because that is what is stored on the event, and it is lenient about
 * surrounding whitespace and casing for the same reason the configuration lookup is.
 */
fun Transfer.transferStatusOf(statusCode: String?): TransferStatus {
    val code = statusCode?.trim()?.takeIf(String::isNotBlank) ?: return TransferStatus.UNKNOWN

    return when {
        code.equals(pendingStatusCode()?.trim(), ignoreCase = true) -> TransferStatus.PENDING
        code.equals(approvedStatusCode()?.trim(), ignoreCase = true) -> TransferStatus.APPROVED
        code.equals(rejectedStatusCode()?.trim(), ignoreCase = true) -> TransferStatus.REJECTED
        else -> TransferStatus.UNKNOWN
    }
}
