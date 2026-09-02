package org.saudigitus.semis.core.data.model.profile

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SubjectPerformanceTest {

    private fun mark(value: String) = ProfileMark(
        eventUid = "event-$value",
        date = null,
        label = "Term",
        value = value.toDoubleOrNull(),
        displayValue = value,
    )

    private fun subject(vararg values: String) = SubjectPerformance(
        programStage = "stage",
        subject = "History",
        marks = values.map(::mark),
    )

    @Test
    fun `the average of a subject is the mean of its marks`() {
        assertEquals(12.0, subject("10", "12", "14").average!!, 0.001)
    }

    @Test
    fun `a subject without marks has no average`() {
        assertNull(subject().average)
    }

    @Test
    fun `marks that are not numbers are left out of the average`() {
        assertEquals(10.0, subject("10", "absent").average!!, 0.001)
    }

    @Test
    fun `a subject whose marks are all unreadable has no average`() {
        assertNull(subject("absent", "n/a").average)
    }
}
