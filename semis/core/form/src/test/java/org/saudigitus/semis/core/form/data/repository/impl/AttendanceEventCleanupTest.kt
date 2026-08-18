package org.saudigitus.semis.core.form.data.repository.impl

import org.junit.Assert.assertEquals
import org.junit.Test
import org.saudigitus.semis.core.designsystem.attendance.model.AttendanceEvent
import org.saudigitus.semis.core.designsystem.attendance.model.AttendanceEventWithDecorator

class AttendanceEventCleanupTest {

    private fun record(tei: String, value: String, eventUid: String? = "event-$tei") =
        AttendanceEventWithDecorator(
            event = AttendanceEvent(
                tei = tei,
                event = eventUid,
                enrollment = "enrollment-$tei",
                dataElement = "status",
                value = value,
                date = "2026-08-18",
            ),
        )

    private val configuredCodes = listOf("ABSENT", "LATE")

    @Test
    fun `with the status flow on only the coded statuses are deleted`() {
        val events = listOf(
            record("a", "ABSENT"),
            record("b", "LATE"),
            record("c", ""),
        )

        val removable = removableAttendanceEvents(
            events = events,
            configuredStatusCodes = configuredCodes,
            absencesOnly = true,
        )

        assertEquals(listOf("a", "b"), removable.map { it.event?.tei })
    }

    @Test
    fun `without the status flow every record of the date is deleted`() {
        val events = listOf(
            record("a", "PRESENT"),
            record("b", "ABSENT"),
            record("c", "LATE"),
        )

        val removable = removableAttendanceEvents(
            events = events,
            configuredStatusCodes = configuredCodes,
            absencesOnly = false,
        )

        assertEquals(listOf("a", "b", "c"), removable.map { it.event?.tei })
    }

    @Test
    fun `a present record is kept while the status flow is on`() {
        val events = listOf(record("a", "PRESENT"))

        val removable = removableAttendanceEvents(
            events = events,
            configuredStatusCodes = configuredCodes,
            absencesOnly = true,
        )

        assertEquals(emptyList<String>(), removable.map { it.event?.tei })
    }

    @Test
    fun `nothing is removable when the date holds no record`() {
        assertEquals(
            emptyList<AttendanceEventWithDecorator>(),
            removableAttendanceEvents(emptyList(), configuredCodes, absencesOnly = true),
        )
        assertEquals(
            emptyList<AttendanceEventWithDecorator>(),
            removableAttendanceEvents(emptyList(), configuredCodes, absencesOnly = false),
        )
    }

    @Test
    fun `a learner dropped from the form leaves an orphan to delete`() {
        val loaded = listOf(record("a", "ABSENT"), record("b", "LATE"))
        val current = listOf(record("a", "ABSENT"))

        val orphans = orphanedAttendanceEvents(loaded = loaded, current = current)

        assertEquals(listOf("b"), orphans.map { it.event?.tei })
    }

    @Test
    fun `a learner whose status changed is not treated as an orphan`() {
        val loaded = listOf(record("a", "ABSENT"))
        val current = listOf(record("a", "LATE"))

        assertEquals(
            emptyList<String>(),
            orphanedAttendanceEvents(loaded = loaded, current = current).map { it.event?.tei },
        )
    }

    @Test
    fun `clearing the form orphans every loaded record`() {
        val loaded = listOf(record("a", "ABSENT"), record("b", "LATE"))

        assertEquals(
            listOf("a", "b"),
            orphanedAttendanceEvents(loaded = loaded, current = emptyList())
                .map { it.event?.tei },
        )
    }
}
