package org.saudigitus.semis.attendance.ui.repository

import org.saudigitus.semis.attendance.ui.model.AttendanceStatus
import org.saudigitus.semis.core.designsystem.components.FilterDetailsState

interface AttendanceRepository {

    suspend fun saveAttendanceStatus(
        orgUnit: String,
        program: String,
        date: String,
        filterDetailsState: FilterDetailsState,
        attendanceStatus: AttendanceStatus
    )
    suspend fun getAttendanceStatus(program: String, date: String): AttendanceStatus?
}