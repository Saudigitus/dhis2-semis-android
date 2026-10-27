package org.saudigitus.semis.attendance.ui.model

import org.hisp.dhis.android.core.event.EventStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AttendanceStatusChoiceTest {

    private val context = listOf(
        "iDSrFrrVgmX" to "2025",
        "kNNoif9gASf" to "Grade 1",
        "RhABRLO2Fae" to "A",
    )

    private fun candidate(
        name: String,
        status: EventStatus? = EventStatus.ACTIVE,
        lastUpdated: Long = 0L,
        values: List<Pair<String, String>> = context,
    ) = AttendanceStatusCandidate(
        event = name,
        status = status,
        lastUpdated = lastUpdated,
        contextValues = values,
    )

    @Test
    fun `the only candidate is the one chosen`() {
        val only = candidate("only")

        assertEquals("only", chooseAttendanceStatus(listOf(only), context)?.event)
    }

    @Test
    fun `a completed day is preferred over one left open`() {
        val open = candidate("open", status = EventStatus.ACTIVE, lastUpdated = 200)
        val completed = candidate("completed", status = EventStatus.COMPLETED, lastUpdated = 100)

        assertEquals("completed", chooseAttendanceStatus(listOf(open, completed), context)?.event)
        assertEquals("completed", chooseAttendanceStatus(listOf(completed, open), context)?.event)
    }

    @Test
    fun `among completed the most recently updated is chosen`() {
        val older = candidate("older", status = EventStatus.COMPLETED, lastUpdated = 100)
        val newer = candidate("newer", status = EventStatus.COMPLETED, lastUpdated = 300)

        assertEquals("newer", chooseAttendanceStatus(listOf(older, newer), context)?.event)
        assertEquals("newer", chooseAttendanceStatus(listOf(newer, older), context)?.event)
    }

    @Test
    fun `among open days the most recently updated is chosen`() {
        val older = candidate("older", lastUpdated = 100)
        val newer = candidate("newer", lastUpdated = 300)

        assertEquals("newer", chooseAttendanceStatus(listOf(older, newer), context)?.event)
    }

    @Test
    fun `the order the candidates arrive in does not change the answer`() {
        val a = candidate("a", status = EventStatus.COMPLETED, lastUpdated = 100)
        val b = candidate("b", status = EventStatus.ACTIVE, lastUpdated = 900)
        val c = candidate("c", status = EventStatus.COMPLETED, lastUpdated = 200)

        val orders = listOf(listOf(a, b, c), listOf(c, b, a), listOf(b, a, c), listOf(b, c, a))

        orders.forEach { order ->
            assertEquals("c", chooseAttendanceStatus(order, context)?.event)
        }
    }

    @Test
    fun `a candidate of another class is not considered`() {
        val otherClass = candidate(
            "other",
            values = listOf("iDSrFrrVgmX" to "2025", "kNNoif9gASf" to "Grade 1", "RhABRLO2Fae" to "B"),
        )

        assertNull(chooseAttendanceStatus(listOf(otherClass), context)?.event)
    }

    @Test
    fun `a candidate carrying more than is asked for still counts`() {
        val richer = candidate("richer", values = context + ("extra" to "value"))

        assertEquals("richer", chooseAttendanceStatus(listOf(richer), context)?.event)
    }

    @Test
    fun `with nothing to identify a class by, nothing is chosen`() {
        val one = candidate("one", values = emptyList())
        val two = candidate("two", values = emptyList())

        assertNull(chooseAttendanceStatus(listOf(one, two), emptyList()))
    }

    @Test
    fun `no candidates at all yields nothing`() {
        assertNull(chooseAttendanceStatus(emptyList<AttendanceStatusCandidate<String>>(), context))
    }
}
