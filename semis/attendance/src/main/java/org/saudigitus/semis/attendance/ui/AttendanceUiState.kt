package org.saudigitus.semis.attendance.ui

import androidx.compose.runtime.Immutable
import org.saudigitus.semis.attendance.ui.model.AttendanceStatus
import org.saudigitus.semis.core.data.model.SearchTeiModel
import org.saudigitus.semis.core.designsystem.attendance.model.AttendanceButtonModel
import org.saudigitus.semis.core.designsystem.components.bottomsheet.model.BottomSheetState
import org.saudigitus.semis.core.designsystem.components.model.ToolbarHeaders
import org.saudigitus.semis.core.designsystem.components.summary.SummaryState
import org.saudigitus.semis.core.form.ui.state.FormBuilderState
import org.saudigitus.semis.core.utils.ButtonStep

@Immutable
data class AttendanceUiState(
    val isLoading: Boolean = false,
    val displayBulk: Boolean = false,
    val displayDialog: Boolean = false,
    val overrideBulk: Boolean = false,
    val toolbarHeaders: ToolbarHeaders = ToolbarHeaders(""),
    val dateValidator: (Long) -> Boolean = { _ -> true },
    val program: String = "",
    val buttonStep: ButtonStep = ButtonStep.NONE,
    val canTakeAttendance: Boolean = true,
    val allowAttendanceStatus: Boolean = false,
    val pendingSyncCount: Int = 0,
    val notRecordedCount: Int = 0,
    /** Whether the selected day already carries saved attendance events. */
    val hasPersistedAttendance: Boolean = false,
    /** Whether attendance has been recorded at all for the selected day. */
    val hasAttendanceRecord: Boolean = false,
    val teis: List<SearchTeiModel> = emptyList(),
    val attendanceStatus: AttendanceStatus? = null,
    val formBuilderState: FormBuilderState = FormBuilderState(),
    val attendanceSummaryState: SummaryState = SummaryState(),
    val bottomSheetState: BottomSheetState.HasItemsState = BottomSheetState.HasItemsState(),
    val genericsBottomSheetState: BottomSheetState.GenericsState<AttendanceButtonModel> = BottomSheetState.GenericsState(),
    val errorMessage: String? = null
)
