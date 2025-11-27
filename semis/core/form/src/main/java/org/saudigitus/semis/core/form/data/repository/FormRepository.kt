package org.saudigitus.semis.core.form.data.repository

import kotlinx.coroutines.flow.StateFlow
import org.saudigitus.semis.core.data.model.SearchTeiModel
import org.saudigitus.semis.core.designsystem.attendance.AttendanceButtonState
import org.saudigitus.semis.core.designsystem.attendance.model.AttendanceButtonModel
import org.saudigitus.semis.core.designsystem.components.bottomsheet.model.BottomSheetModel
import org.saudigitus.semis.core.form.data.model.FormFieldState

interface FormRepository {

    val attendanceButtonStateFlow: StateFlow<AttendanceButtonState>

    fun allowFormEdition(enabled: Boolean)
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

    suspend fun getFormFields(
        program: String,
        stage: String,
        dl: String? = null
    ): List<FormFieldState>

    suspend fun applyProgramRules(
        orgUnit: String,
        program: String,
        programStage: String,
        fields: List<FormFieldState>,
    ): List<FormFieldState>

    suspend fun attendanceSummary(
        program: String,
        getSummaries: (List<BottomSheetModel>) -> Unit
    )

    fun reset()
}