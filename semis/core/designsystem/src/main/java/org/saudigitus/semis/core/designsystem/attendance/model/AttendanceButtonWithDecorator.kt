package org.saudigitus.semis.core.designsystem.attendance.model

data class AttendanceButtonWithDecorator(
    val button: AttendanceButtonModel? = null,
    val decorator: AttendanceButtonDecorator? = null
) {
    override fun toString(): String {
        return "AttendanceButtonWithDecorator(button=$button, decorator=$decorator)"
    }
}
