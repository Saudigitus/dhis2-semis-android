package org.saudigitus.semis.core.data.model.app_config

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.saudigitus.semis.core.utils.JsonMapper

class TransferTest {

    @Test
    fun `reads corrected pending status property name`() {
        val transfer = decodeTransfer("pendingCode")

        assertEquals("PENDING", transfer.pendingCode)
    }

    @Test
    fun `reads legacy pending status property name`() {
        val transfer = decodeTransfer("penddingCode")

        assertEquals("PENDING", transfer.pendingCode)
    }

    @Test
    fun `gets pending value from the matching status option code`() {
        val transfer = decodeTransfer("pendingCode").copy(
            statusOptions = listOf(
                statusOption(code = "APPROVED_CODE", key = "approved"),
                statusOption(code = "PENDING_CODE", key = " pending "),
            )
        )

        assertEquals("PENDING_CODE", transfer.pendingStatusCode())
    }

    @Test
    fun `does not use the legacy pending property as the status value`() {
        val transfer = decodeTransfer("pendingCode")

        assertEquals(null, transfer.pendingStatusCode())
    }

    @Test
    fun `configuration only requires the stage and the destination data element`() {
        val configured = decodeTransfer("pendingCode")

        assertTrue(configured.isTransferEnabledAndConfigured())
        assertTrue(configured.copy(pendingCode = null, status = null).isTransferEnabledAndConfigured())
        assertFalse(configured.copy(destinySchool = null).isTransferEnabledAndConfigured())
        assertFalse(configured.copy(programStage = null).isTransferEnabledAndConfigured())
        assertFalse(configured.copy(enabled = false).isTransferEnabledAndConfigured())
        assertFalse(null.isTransferEnabledAndConfigured())
    }

    @Test
    fun `origin school key never takes part in deciding the configuration is usable`() {
        val configured = decodeTransfer("pendingCode")

        assertTrue(configured.copy(originSchool = null).isTransferEnabledAndConfigured())
        assertTrue(
            configured.copy(originSchool = configured.destinySchool).isTransferEnabledAndConfigured(),
        )
    }

    private fun statusOption(code: String, key: String) = StatusOption(
        code = code,
        color = null,
        configKey = null,
        icon = null,
        key = key,
        totalSummary = null,
    )

    private fun decodeTransfer(pendingProperty: String): Transfer =
        JsonMapper.json.decodeFromString(
            """{
                "approvedCode":"APPROVED",
                "destinySchool":"destination",
                "enabled":true,
                "lastUpdate":"updatedAt",
                "originSchool":"origin",
                "$pendingProperty":"PENDING",
                "programStage":"stage",
                "reprovedCode":"REJECTED",
                "status":"status",
                "statusOptions":[]
            }""".trimIndent()
        )
}
