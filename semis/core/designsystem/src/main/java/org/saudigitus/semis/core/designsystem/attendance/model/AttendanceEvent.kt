package org.saudigitus.semis.core.designsystem.attendance.model

data class AttendanceEvent(
    val tei: String,
    val enrollment: String,
    val event: String? = null,
    val dataElement: String,
    val reasonDataElement: String? = null,
    val reasonOfAbsence: String? = null,
    val value: String,
    val date: String,
)