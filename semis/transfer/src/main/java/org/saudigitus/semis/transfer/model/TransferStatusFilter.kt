package org.saudigitus.semis.transfer.model

import org.saudigitus.semis.core.data.model.transfer.TeiTransfer
import org.saudigitus.semis.core.data.model.transfer.TransferStatus

/**
 * The status a list is narrowed to. Absence of a filter means every request is shown,
 * which is what the lists open on.
 */
enum class TransferStatusFilter(val status: TransferStatus) {
    PENDING(TransferStatus.PENDING),
    APPROVED(TransferStatus.APPROVED),
    REJECTED(TransferStatus.REJECTED),
}

/**
 * Requests carrying a status the configuration no longer knows are only reachable with
 * no filter applied, so a misconfiguration never hides them behind a chip nobody can
 * select.
 */
internal fun List<TeiTransfer>.filterBy(filter: TransferStatusFilter?): List<TeiTransfer> =
    filter?.let { selected -> filter { it.status == selected.status } } ?: this
