package org.saudigitus.semis.core.form.ui

import org.hisp.dhis.android.core.common.ValueType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.saudigitus.semis.core.data.model.OptionModel
import org.saudigitus.semis.core.designsystem.attendance.AttendanceButtonState
import org.saudigitus.semis.core.designsystem.attendance.model.AttendanceEvent
import org.saudigitus.semis.core.designsystem.attendance.model.AttendanceEventWithDecorator
import org.saudigitus.semis.core.form.data.model.FormFieldState

class AttendanceReasonFieldDataTest {

    @Test
    fun `restores selected option for saved absence reason`() {
        val illness = OptionModel(
            uid = "illness",
            code = "ILLNESS",
            displayName = "Illness",
            sortOrder = 1,
        )

        val fieldData = attendanceReasonFieldData(
            key = "learner",
            field = reasonField(options = listOf(illness)),
            attendanceState = attendanceState(reason = "ILLNESS"),
        )

        assertEquals("ILLNESS", fieldData?.value)
        assertEquals(illness, fieldData?.optionModel)
    }

    @Test
    fun `restores saved free text absence reason`() {
        val fieldData = attendanceReasonFieldData(
            key = "learner",
            field = reasonField(),
            attendanceState = attendanceState(reason = "Medical appointment"),
        )

        assertEquals("Medical appointment", fieldData?.value)
        assertNull(fieldData?.optionModel)
    }

    private fun reasonField(options: List<OptionModel>? = null) = FormFieldState(
        dataElementUid = "reason",
        label = "Reason of absence",
        valueType = ValueType.TEXT,
        optionSet = options,
        isAttendanceReason = true,
    )

    private fun attendanceState(reason: String) = AttendanceButtonState(
        attendanceEvents = listOf(
            AttendanceEventWithDecorator(
                event = AttendanceEvent(
                    tei = "learner",
                    event = "event",
                    enrollment = "enrollment",
                    dataElement = "status",
                    value = "ABS",
                    reasonDataElement = "reason",
                    reasonOfAbsence = reason,
                    date = "2026-08-13",
                ),
            )
        ),
    )
}
