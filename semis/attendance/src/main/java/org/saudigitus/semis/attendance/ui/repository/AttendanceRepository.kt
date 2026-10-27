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

    /**
     * Rewrites the status counters of the day without closing the request, so a reset or
     * an edit leaves the attendance status event consistent with what is recorded.
     */
    suspend fun updateAttendanceStatusSummary(
        orgUnit: String,
        program: String,
        date: String,
        filterDetailsState: FilterDetailsState,
        totalLearners: Int,
        attendanceEvents: List<AttendanceEventWithDecorator>,
    ): AttendanceStatus?

    /**
     * The configured filter values that say which class a record belongs to.
     *
     * The same values are written on the class event and on each learner event, so that a report
     * built from the learner stage and the totals held on the class event cannot disagree. Which
     * values these are comes from the configuration, not from this code.
     */
    suspend fun classContextValues(
        program: String,
        filterDetailsState: FilterDetailsState,
    ): List<Pair<String, String>>

    suspend fun getAttendanceStatus(
        orgUnit: String,
        program: String,
        date: String,
        filterDetailsState: FilterDetailsState,
    ): AttendanceStatus?
}
