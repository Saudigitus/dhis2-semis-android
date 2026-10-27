package org.saudigitus.semis.core.form.ui

import org.saudigitus.semis.core.designsystem.attendance.AttendanceButtonState
import org.saudigitus.semis.core.form.data.model.FormFieldData
import org.saudigitus.semis.core.form.data.model.FormFieldState

internal fun attendanceReasonFieldData(
    key: String,
    field: FormFieldState,
    attendanceState: AttendanceButtonState,
): FormFieldData? {
    val attendanceEvent = attendanceState.attendanceEvents
        .find { it.event?.tei == key }
        ?.event
        ?: return null
    val reason = attendanceEvent.reasonOfAbsence

    return FormFieldData(
        tei = key,
        event = attendanceEvent.event,
        dataElement = field.dataElementUid,
        value = reason,
        optionModel = field.optionSet?.find { it.code == reason },
    )
}
