package org.saudigitus.semis.core.data.model.transfer

import org.junit.Assert.assertEquals
import org.junit.Test
import org.saudigitus.semis.core.data.model.app_config.StatusOption
import org.saudigitus.semis.core.data.model.app_config.Transfer

class TransferStatusTest {

    @Test
    fun `each configured code maps to its status`() {
        assertEquals(TransferStatus.PENDING, transfer.transferStatusOf("Pending"))
        assertEquals(TransferStatus.APPROVED, transfer.transferStatusOf("Approved"))
        assertEquals(TransferStatus.REJECTED, transfer.transferStatusOf("Reproved"))
    }

    @Test
    fun `codes are matched ignoring case and surrounding blanks`() {
        assertEquals(TransferStatus.PENDING, transfer.transferStatusOf("  pending "))
        assertEquals(TransferStatus.APPROVED, transfer.transferStatusOf("APPROVED"))
    }

    @Test
    fun `a code the configuration no longer knows stays visible as unknown`() {
        assertEquals(TransferStatus.UNKNOWN, transfer.transferStatusOf("Withdrawn"))
    }

    @Test
    fun `a request carrying no status reads as unknown rather than pending`() {
        assertEquals(TransferStatus.UNKNOWN, transfer.transferStatusOf(null))
        assertEquals(TransferStatus.UNKNOWN, transfer.transferStatusOf("   "))
    }

    @Test
    fun `falls back to the legacy top level codes when a status option is absent`() {
        val legacy = transfer.copy(
            statusOptions = listOf(statusOption("Pending", "pending")),
            approvedCode = "OK",
            reprovedCode = "NO",
        )

        assertEquals(TransferStatus.APPROVED, legacy.transferStatusOf("OK"))
        assertEquals(TransferStatus.REJECTED, legacy.transferStatusOf("NO"))
    }

    private val transfer = Transfer(
        approvedCode = null,
        destinySchool = "destination",
        enabled = true,
        lastUpdate = null,
        originSchool = null,
        pendingCode = null,
        programStage = "stage",
        reprovedCode = null,
        status = "status",
        statusOptions = listOf(
            statusOption("Approved", "approved"),
            statusOption("Pending", "pending"),
            statusOption("Reproved", "reproved"),
        ),
    )

    private fun statusOption(code: String, key: String) = StatusOption(
        code = code,
        color = null,
        configKey = null,
        icon = null,
        key = key,
        totalSummary = null,
    )
}
