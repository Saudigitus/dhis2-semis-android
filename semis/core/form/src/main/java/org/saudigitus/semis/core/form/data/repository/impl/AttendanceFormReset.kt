package org.saudigitus.semis.core.form.data.repository.impl

import org.saudigitus.semis.core.designsystem.attendance.AttendanceButtonState

internal fun resetAttendanceFormState(
    current: AttendanceButtonState,
    loaded: AttendanceButtonState,
): AttendanceButtonState = loaded.copy(isEditing = current.isEditing)
