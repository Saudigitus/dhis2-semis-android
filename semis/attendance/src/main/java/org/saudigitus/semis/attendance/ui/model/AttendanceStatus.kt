package org.saudigitus.semis.attendance.ui.model

data class AttendanceStatus(
    val event: String? = null,
    val program: String?,
    val programStage: String?,
    val dataElement: String?,
    val displayName: String?,
    val value: String? = null,
)
