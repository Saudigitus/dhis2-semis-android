package org.saudigitus.semis.attendance.data.repository

import org.saudigitus.semis.core.designsystem.attendance.model.AttendanceButtonModel

interface AttendanceOptionRepository {
    suspend fun getAttendanceStatusOptions(program: String): List<AttendanceButtonModel>
}