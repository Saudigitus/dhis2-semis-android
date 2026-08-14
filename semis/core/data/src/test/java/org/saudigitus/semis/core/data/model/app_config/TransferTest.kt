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
                StatusOption("APPROVED_CODE", null, null, null, "approved"),
                StatusOption("PENDING_CODE", null, null, null, " pending "),
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
    fun `incoming configuration only requires the transfer event fields`() {
        val configured = decodeTransfer("pendingCode")

        assertTrue(configured.isIncomingEnabledAndConfigured())
        assertTrue(configured.copy(pendingCode = null, status = null).isIncomingEnabledAndConfigured())
        assertFalse(configured.copy(originSchool = null).isIncomingEnabledAndConfigured())
        assertFalse(configured.copy(destinySchool = null).isIncomingEnabledAndConfigured())
        assertFalse(null.isIncomingEnabledAndConfigured())
    }

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
