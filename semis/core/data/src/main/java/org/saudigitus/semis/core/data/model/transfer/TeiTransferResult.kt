package org.saudigitus.semis.core.data.model.transfer

data class TeiTransferResult(
    val transferredTeiUids: List<String>,
    val failures: List<TeiTransferFailure>,
) {
    val isSuccessful: Boolean
        get() = failures.isEmpty() && transferredTeiUids.isNotEmpty()
}
