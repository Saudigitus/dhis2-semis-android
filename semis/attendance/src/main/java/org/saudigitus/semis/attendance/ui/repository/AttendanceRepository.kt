package org.saudigitus.semis.attendance.ui.repository

import org.saudigitus.semis.attendance.ui.model.AttendanceStatus
import org.saudigitus.semis.core.designsystem.components.FilterDetailsState
import org.saudigitus.semis.core.designsystem.attendance.model.AttendanceEventWithDecorator

interface AttendanceRepository {

    suspend fun createAttendanceStatus(
        orgUnit: String,
        program: String,
        date: String,
        filterDetailsState: FilterDetailsState,
    ): AttendanceStatus?

    suspend fun completeAttendanceStatus(
        orgUnit: String,
        program: String,
        date: String,
        filterDetailsState: FilterDetailsState,
        totalLearners: Int,
        attendanceEvents: List<AttendanceEventWithDecorator>,
    ): AttendanceStatus?

    suspend fun getAttendanceStatus(
        orgUnit: String,
        program: String,
        date: String,
        filterDetailsState: FilterDetailsState,
    ): AttendanceStatus?
}
