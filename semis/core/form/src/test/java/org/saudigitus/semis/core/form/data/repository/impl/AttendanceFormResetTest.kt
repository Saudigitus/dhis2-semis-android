package org.saudigitus.semis.core.form.data.repository.impl

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.saudigitus.semis.core.designsystem.attendance.AttendanceButtonState
import org.saudigitus.semis.core.designsystem.attendance.model.AttendanceButtonModel
import org.saudigitus.semis.core.designsystem.attendance.model.AttendanceEvent
import org.saudigitus.semis.core.designsystem.attendance.model.AttendanceEventWithDecorator

class AttendanceFormResetTest {

    @Test
    fun `reset restores loaded values and keeps editing enabled`() {
        val loaded = AttendanceButtonState(
            buttons = listOf(AttendanceButtonModel(key = "present", code = "PRESENT")),
            attendanceEvents = listOf(event("learner-1", "PRESENT")),
        )
        val current = loaded.copy(
            isEditing = true,
            attendanceEvents = listOf(event("learner-1", "ABSENT")),
        )

        val reset = resetAttendanceFormState(current, loaded)

        assertEquals("PRESENT", reset.attendanceEvents.single().event?.value)
        assertEquals(loaded.buttons, reset.buttons)
        assertTrue(reset.isEditing)
    }

    private fun event(learnerUid: String, value: String) = AttendanceEventWithDecorator(
        event = AttendanceEvent(
            tei = learnerUid,
            enrollment = "enrollment",
            dataElement = "attendance-status",
            value = value,
            date = "2026-08-15",
        )
    )
}
