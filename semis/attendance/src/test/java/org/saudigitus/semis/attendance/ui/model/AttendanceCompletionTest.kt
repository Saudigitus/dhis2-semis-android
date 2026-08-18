package org.saudigitus.semis.attendance.ui.model

import org.junit.Assert.assertEquals
import org.junit.Test
import org.saudigitus.semis.core.designsystem.attendance.model.AttendanceEvent
import org.saudigitus.semis.core.designsystem.attendance.model.AttendanceEventWithDecorator

class AttendanceCompletionTest {

    @Test
    fun `counts learners without an attendance status`() {
        val missing = missingLearnerAttendanceCount(
            learnerUids = listOf("learner-1", "learner-2", "learner-3"),
            attendanceEvents = listOf(event("learner-1"), event("learner-2")),
        )

        assertEquals(1, missing)
    }

    @Test
    fun `requires a non-empty attendance status`() {
        val missing = missingLearnerAttendanceCount(
            learnerUids = listOf("learner-1"),
            attendanceEvents = listOf(event("learner-1", value = "")),
        )

        assertEquals(1, missing)
    }

    @Test
    fun `ignores duplicate and unrelated learner events`() {
        val missing = missingLearnerAttendanceCount(
            learnerUids = listOf("learner-1", "learner-2"),
            attendanceEvents = listOf(
                event("learner-1"),
                event("learner-1"),
                event("another-learner"),
            ),
        )

        assertEquals(1, missing)
    }

    private fun event(
        learnerUid: String,
        value: String = "PRESENT",
    ) = AttendanceEventWithDecorator(
        event = AttendanceEvent(
            tei = learnerUid,
            enrollment = "enrollment",
            dataElement = "attendance-status",
            value = value,
            date = "2026-08-15",
        )
    )
}
