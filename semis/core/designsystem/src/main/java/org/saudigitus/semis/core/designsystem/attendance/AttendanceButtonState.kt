package org.saudigitus.semis.core.designsystem.attendance

import androidx.compose.runtime.Immutable
import org.saudigitus.semis.core.designsystem.attendance.model.AttendanceButtonDecorator
import org.saudigitus.semis.core.designsystem.attendance.model.AttendanceButtonModel
import org.saudigitus.semis.core.designsystem.attendance.model.AttendanceButtonWithDecorator
import org.saudigitus.semis.core.designsystem.attendance.model.AttendanceEventWithDecorator
import org.saudigitus.semis.core.utils.Utils

@Immutable
data class AttendanceButtonState(
    val key: String = Utils.generateRandomId(),
    val isLoading: Boolean = true,
    val isEditing: Boolean = false,
    val buttons: List<AttendanceButtonModel> = emptyList(),
    val attendanceEvents: List<AttendanceEventWithDecorator> = emptyList(),
)