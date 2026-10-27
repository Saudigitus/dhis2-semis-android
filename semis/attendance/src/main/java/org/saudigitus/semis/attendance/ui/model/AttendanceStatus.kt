package org.saudigitus.semis.attendance.ui.model

import org.hisp.dhis.android.core.event.EventStatus

data class AttendanceStatus(
    val event: String,
    val program: String?,
    val programStage: String?,
    val status: EventStatus?,
)
