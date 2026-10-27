package org.saudigitus.semis.core.form.ui

import org.hisp.dhis.android.core.common.ValueType
import org.junit.Assert.assertEquals
import org.junit.Test
import org.saudigitus.semis.core.designsystem.attendance.AttendanceButtonState
import org.saudigitus.semis.core.designsystem.attendance.model.AttendanceButtonModel
import org.saudigitus.semis.core.designsystem.attendance.model.AttendanceEvent
import org.saudigitus.semis.core.designsystem.attendance.model.AttendanceEventWithDecorator
import org.saudigitus.semis.core.form.data.model.FormFieldState

class AttendanceCompulsoryFieldsTest {

    private val status = FormFieldState(
        dataElementUid = "status",
        label = "Current attendance status",
        valueType = ValueType.TEXT,
        isAttendanceType = true,
        mandatory = true,
    )
    private val reason = FormFieldState(
        dataElementUid = "reason",
        label = "Reason of absence",
        valueType = ValueType.TEXT,
        isAttendanceReason = true,
    )
    private val compulsoryReason = reason.copy(mandatory = true)
    private val buttons = listOf(
        AttendanceButtonModel(key = "present"),
        AttendanceButtonModel(key = "absent", code = "ABS", isAbsence = true),
        AttendanceButtonModel(key = "late", code = "LATE"),
    )

    @Test
    fun `an absent learner without a compulsory reason cannot be saved`() {
        val missing = learnersMissingCompulsoryAttendanceFields(
            fields = listOf(status, compulsoryReason),
            attendanceState = attendanceState("a" to ("ABS" to null), "b" to ("ABS" to "SICK")),
        )

        assertEquals(setOf("a"), missing)
    }

    @Test
    fun `a reason the metadata leaves optional never blocks the save`() {
        val missing = learnersMissingCompulsoryAttendanceFields(
            fields = listOf(status, reason),
            attendanceState = attendanceState("a" to ("ABS" to null)),
        )

        assertEquals(emptySet<String>(), missing)
    }

    @Test
    fun `a learner the reason is not asked of is never missing it`() {
        val missing = learnersMissingCompulsoryAttendanceFields(
            fields = listOf(status, compulsoryReason),
            attendanceState = attendanceState("a" to ("LATE" to null)),
        )

        assertEquals(emptySet<String>(), missing)
    }

    @Test
    fun `a blank reason counts as missing`() {
        val state = attendanceState("a" to ("ABS" to " "))

        assertEquals(true, isMissingCompulsoryAttendanceField("a", compulsoryReason, state))
        assertEquals(false, isMissingCompulsoryAttendanceField("a", status, state))
    }

    private fun attendanceState(vararg learners: Pair<String, Pair<String, String?>>) =
        AttendanceButtonState(
            buttons = buttons,
            attendanceEvents = learners.map { (tei, record) ->
                AttendanceEventWithDecorator(
                    event = AttendanceEvent(
                        tei = tei,
                        enrollment = "enrollment-$tei",
                        dataElement = "status",
                        value = record.first,
                        reasonOfAbsence = record.second,
                        date = "2026-10-20",
                    ),
                )
            },
        )
}
