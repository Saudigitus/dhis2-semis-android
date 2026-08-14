package org.saudigitus.semis.core.form.ui

import org.hisp.dhis.android.core.common.ValueType
import org.junit.Assert.assertEquals
import org.junit.Test
import org.saudigitus.semis.core.designsystem.attendance.AttendanceButtonState
import org.saudigitus.semis.core.designsystem.attendance.model.AttendanceButtonModel
import org.saudigitus.semis.core.designsystem.attendance.model.AttendanceEvent
import org.saudigitus.semis.core.designsystem.attendance.model.AttendanceEventWithDecorator
import org.saudigitus.semis.core.form.data.model.FormFieldState

class AttendanceFieldVisibilityTest {

    private val status = FormFieldState(
        dataElementUid = "status",
        label = "Current attendance status",
        valueType = ValueType.TEXT,
        isAttendanceType = true,
    )
    private val reason = FormFieldState(
        dataElementUid = "reason",
        label = "Reason of absence",
        valueType = ValueType.TEXT,
        isAttendanceReason = true,
    )
    private val absentDays = FormFieldState(
        dataElementUid = "days",
        label = "Absent days",
        valueType = ValueType.INTEGER,
    )
    private val buttons = listOf(
        AttendanceButtonModel(key = "present"),
        AttendanceButtonModel(key = "absent", code = "ABS", isAbsence = true),
        AttendanceButtonModel(key = "late", code = "LATE"),
    )

    @Test
    fun `shows only attendance status when learner is present`() {
        val visible = visibleAttendanceFields(
            key = "learner",
            fields = listOf(status, reason, absentDays),
            attendanceState = AttendanceButtonState(buttons = buttons),
        )

        assertEquals(listOf(status), visible)
    }

    @Test
    fun `shows absence reason only for absence status`() {
        val visible = visibleAttendanceFields(
            key = "learner",
            fields = listOf(status, reason, absentDays),
            attendanceState = attendanceState("ABS"),
        )

        assertEquals(listOf(status, reason), visible)
    }

    @Test
    fun `does not show absence reason for another non-present status`() {
        val visible = visibleAttendanceFields(
            key = "learner",
            fields = listOf(status, reason, absentDays),
            attendanceState = attendanceState("LATE"),
        )

        assertEquals(listOf(status), visible)
    }

    private fun attendanceState(value: String) = AttendanceButtonState(
        buttons = buttons,
        attendanceEvents = listOf(
            AttendanceEventWithDecorator(
                event = AttendanceEvent(
                    tei = "learner",
                    enrollment = "enrollment",
                    dataElement = "status",
                    value = value,
                    date = "2026-08-13",
                ),
            )
        ),
    )
}
