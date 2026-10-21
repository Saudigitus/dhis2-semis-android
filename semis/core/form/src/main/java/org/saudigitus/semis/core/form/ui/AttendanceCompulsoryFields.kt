package org.saudigitus.semis.core.form.ui

import org.saudigitus.semis.core.designsystem.attendance.AttendanceButtonState
import org.saudigitus.semis.core.form.data.model.FormFieldState

/**
 * Whether the register leaves empty, for this learner, a field the metadata makes compulsory.
 *
 * Only a field the register shows for the learner counts: the reason of absence is asked of
 * absent learners alone, so a present learner never misses it, however compulsory it is. The
 * status itself is not checked here, a learner holding a record always has one.
 */
internal fun isMissingCompulsoryAttendanceField(
    key: String,
    field: FormFieldState,
    attendanceState: AttendanceButtonState,
): Boolean {
    if (!field.mandatory || !field.isAttendanceReason) return false
    if (field !in visibleAttendanceFields(key, listOf(field), attendanceState)) return false

    val reason = attendanceState.attendanceEvents
        .find { it.event?.tei == key }
        ?.event
        ?.reasonOfAbsence

    return reason.isNullOrBlank()
}

/**
 * The learners the register cannot be saved for until a compulsory field is filled.
 */
internal fun learnersMissingCompulsoryAttendanceFields(
    fields: List<FormFieldState>,
    attendanceState: AttendanceButtonState,
): Set<String> = attendanceState.attendanceEvents
    .mapNotNull { it.event?.tei }
    .filter { tei ->
        fields.any { field -> isMissingCompulsoryAttendanceField(tei, field, attendanceState) }
    }
    .toSet()
