package org.saudigitus.semis.core.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RecordOrderTest {

    private val records = listOf("Zainab", "amadu", null, "Bintu", "")

    @Test
    fun `the configured attribute and direction are read`() {
        val order = recordOrderOf("gz8w04YBSS0:asc")

        assertEquals("gz8w04YBSS0", order?.attribute)
        assertEquals(false, order?.descending)
    }

    @Test
    fun `a descending direction is read`() {
        assertEquals(true, recordOrderOf("gz8w04YBSS0:DESC")?.descending)
    }

    @Test
    fun `an attribute with no direction is ordered ascending`() {
        val order = recordOrderOf("gz8w04YBSS0")

        assertEquals("gz8w04YBSS0", order?.attribute)
        assertEquals(false, order?.descending)
    }

    @Test
    fun `a setting the deployment did not state leaves the order unconfigured`() {
        assertNull(recordOrderOf(null))
        assertNull(recordOrderOf(""))
        assertNull(recordOrderOf(":asc"))
    }

    @Test
    fun `a setting that cannot be read leaves the order unconfigured`() {
        assertNull(recordOrderOf("gz8w04YBSS0:sideways"))
        assertNull(recordOrderOf("gz8w04YBSS0:asc:extra"))
    }

    @Test
    fun `records are ordered ignoring case`() {
        val ordered = records.orderedBy(recordOrderOf("uid:asc")) { it }

        assertEquals(listOf("amadu", "Bintu", "Zainab"), ordered.take(3))
    }

    @Test
    fun `records without a value are listed last whichever direction is asked for`() {
        val ascending = records.orderedBy(recordOrderOf("uid:asc")) { it }
        val descending = records.orderedBy(recordOrderOf("uid:desc")) { it }

        assertEquals(setOf(null, ""), ascending.takeLast(2).toSet())
        assertEquals(setOf(null, ""), descending.takeLast(2).toSet())
    }

    @Test
    fun `a descending order reverses the named records`() {
        val ordered = records.orderedBy(recordOrderOf("uid:desc")) { it }

        assertEquals(listOf("Zainab", "Bintu", "amadu"), ordered.take(3))
    }

    @Test
    fun `an unconfigured order keeps the records as they came`() {
        assertEquals(records, records.orderedBy(null) { it })
    }
}
