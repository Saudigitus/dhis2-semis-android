package org.saudigitus.semis.core.form.data.repository

import kotlinx.coroutines.flow.StateFlow
import org.saudigitus.semis.core.data.model.SearchTeiModel
import org.saudigitus.semis.core.designsystem.attendance.AttendanceButtonState
import org.saudigitus.semis.core.designsystem.attendance.model.AttendanceButtonModel
import org.saudigitus.semis.core.designsystem.attendance.model.AttendanceEventWithDecorator
import org.saudigitus.semis.core.designsystem.components.bottomsheet.model.BottomSheetModel

interface AttendanceEventRepository {

    val attendanceButtonStateFlow: StateFlow<AttendanceButtonState>

    /**
     * @param orgUnit the school of the class being marked, which is where every learner record is
     * written. The learner's own registration unit is never used: after a transfer it names the
     * school the learner came from, and a record written there belongs to somebody else and cannot
     * even be sent by the school taking the attendance.
     * @param contextValues the configured values saying which class the records belong to, written
     * on each learner event where the stage holds them. What the app counts on the device does not
     * depend on them; they are stored so that a report built elsewhere can group by them.
     */
    suspend fun saveAttendance(
        orgUnit: String,
        program: String,
        programStage: String,
        attendanceEvents: List<AttendanceEventWithDecorator>,
        contextValues: List<Pair<String, String>> = emptyList(),
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

    /**
     * Deletes the attendance recorded for the loaded date.
     *
     * @param absencesOnly keeps the records whose value is not one of the configured
     * coded statuses. The attendance status event lives on its own program stage and is
     * never touched here.
     */
    suspend fun deleteAttendance(absencesOnly: Boolean): AttendanceButtonState

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
        totalLearners: Int,
        getSummaries: (List<BottomSheetModel>) -> Unit
    )
}
