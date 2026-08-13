package org.saudigitus.semis.core.form.ui

import org.saudigitus.semis.core.designsystem.attendance.AttendanceButtonState
import org.saudigitus.semis.core.form.data.model.FormFieldState

internal fun visibleAttendanceFields(
    key: String,
    fields: List<FormFieldState>,
    attendanceState: AttendanceButtonState,
): List<FormFieldState> {
    val attendanceEvent = attendanceState.attendanceEvents.find { it.event?.tei == key }
    val isAbsent = attendanceState.buttons.any {
        it.isAbsence && it.code == attendanceEvent?.event?.value
    }

    return fields.filter {
        it.isAttendanceType || it.isAttendanceReason && isAbsent
    }
}
