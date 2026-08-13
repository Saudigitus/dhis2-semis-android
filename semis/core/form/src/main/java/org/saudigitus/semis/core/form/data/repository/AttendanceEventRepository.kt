package org.saudigitus.semis.core.form.data.repository

import kotlinx.coroutines.flow.StateFlow
import org.saudigitus.semis.core.data.model.SearchTeiModel
import org.saudigitus.semis.core.designsystem.attendance.AttendanceButtonState
import org.saudigitus.semis.core.designsystem.attendance.model.AttendanceButtonModel
import org.saudigitus.semis.core.designsystem.attendance.model.AttendanceEventWithDecorator
import org.saudigitus.semis.core.designsystem.components.bottomsheet.model.BottomSheetModel

interface AttendanceEventRepository {

    val attendanceButtonStateFlow: StateFlow<AttendanceButtonState>

    suspend fun saveAttendance(
        program: String,
        programStage: String,
        attendanceEvents: List<AttendanceEventWithDecorator>
    )

    suspend fun saveAttendanceStatus(
        event: String? = null,
        orgUnit: String,
        program: String,
        programStage: String,
        data: List<Pair<String, String?>>,
        eventDate: String
    )

    suspend fun getAttendanceEvent(
        teiUids: List<String>,
        program: String,
        programStage: String,
        dataElement: String,
        reasonDataElement: String,
        eventDate: String?
    ): List<AttendanceEventWithDecorator>

    suspend fun updateAttendanceEvent(
        eventDate: String?,
        tei: SearchTeiModel?,
        buttonModel: AttendanceButtonModel
    ): AttendanceButtonState

    fun updateAttendanceReason(tei: String, dataElement: String, value: String): AttendanceButtonState?

    suspend fun loadAttendanceEvents(
        teiUids: List<String>,
        program: String,
        programStage: String,
        dataElement: String,
        reasonDataElement: String,
        eventDate: String?
    ): AttendanceButtonState

    suspend fun attendanceSummary(
        program: String,
        getSummaries: (List<BottomSheetModel>) -> Unit
    )
}