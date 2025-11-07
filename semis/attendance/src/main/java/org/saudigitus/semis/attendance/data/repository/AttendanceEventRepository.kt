package org.saudigitus.semis.attendance.data.repository

import org.saudigitus.semis.core.designsystem.attendance.model.AttendanceEventWithDecorator

interface AttendanceEventRepository {
    suspend fun save(
        program: String,
        programStage: String,
        attendanceEvents: List<AttendanceEventWithDecorator>
    )

    suspend fun getAttendanceEvent(
        teiUids: List<String>,
        program: String,
        programStage: String,
        dataElement: String,
        reasonDataElement: String,
        eventDate: String?
    ): List<AttendanceEventWithDecorator>
}