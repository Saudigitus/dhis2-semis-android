package org.saudigitus.semis.core.data.model.app_config

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.saudigitus.semis.core.utils.JsonMapper

class StatusOptionTest {

    @Test
    fun `reads the data element each status stores its summary in`() {
        val option = JsonMapper.json.decodeFromString<StatusOption>(
            """{"code":"ABSENT","key":"absent","totalSummary":"de_absent"}"""
        )

        assertEquals("de_absent", option.totalSummary)
    }

    @Test
    fun `a status configured without a summary data element decodes to null`() {
        val option = JsonMapper.json.decodeFromString<StatusOption>(
            """{"code":"LATE","key":"late"}"""
        )

        assertNull(option.totalSummary)
    }

    @Test
    fun `resolves a status code by its configured key`() {
        val transfer = JsonMapper.json.decodeFromString<Transfer>(
            """{
                "enabled":true,
                "statusOptions":[
                    {"code":"PENDING_CODE","key":"pending"},
                    {"code":"APPROVED_CODE","key":"approved"},
                    {"code":"REJECTED_CODE","key":"rejected"}
                ]
            }""".trimIndent()
        )

        assertEquals("PENDING_CODE", transfer.statusCodeFor("pending"))
        assertEquals("APPROVED_CODE", transfer.statusCodeFor("APPROVED"))
        assertEquals("REJECTED_CODE", transfer.statusCodeFor("rejected"))
        assertNull(transfer.statusCodeFor("unknown"))
    }

    @Test
    fun `resolves the approve and reject codes a decision writes`() {
        val transfer = JsonMapper.json.decodeFromString<Transfer>(
            """{
                "enabled":true,
                "statusOptions":[
                    {"code":"Approved","configKey":"approvedCode","key":"approved"},
                    {"code":"Pending","configKey":"penddingCode","key":"pending"},
                    {"code":"Reproved","configKey":"reprovedCode","key":"reproved"}
                ]
            }""".trimIndent()
        )

        assertEquals("Approved", transfer.approvedStatusCode())
        assertEquals("Reproved", transfer.rejectedStatusCode())
        assertEquals("Pending", transfer.pendingStatusCode())
    }

    @Test
    fun `falls back to the legacy code properties when no status option matches`() {
        val transfer = JsonMapper.json.decodeFromString<Transfer>(
            """{
                "enabled":true,
                "approvedCode":"LEGACY_APPROVED",
                "reprovedCode":"LEGACY_REPROVED",
                "statusOptions":[{"code":"Pending","key":"pending"}]
            }""".trimIndent()
        )

        assertEquals("LEGACY_APPROVED", transfer.approvedStatusCode())
        assertEquals("LEGACY_REPROVED", transfer.rejectedStatusCode())
    }
}
