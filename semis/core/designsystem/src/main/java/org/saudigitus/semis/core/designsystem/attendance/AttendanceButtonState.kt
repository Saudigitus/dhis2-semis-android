package org.saudigitus.semis.core.designsystem.attendance

import androidx.compose.runtime.Immutable
import org.saudigitus.semis.core.designsystem.attendance.model.AttendanceButtonDecorator
import org.saudigitus.semis.core.designsystem.attendance.model.AttendanceButtonModel

@Immutable
data class AttendanceButtonState(
    val key: String = "",
    val buttons: List<AttendanceButtonModel> = emptyList(),
    val buttonDecorators: List<AttendanceButtonDecorator> = emptyList()
)