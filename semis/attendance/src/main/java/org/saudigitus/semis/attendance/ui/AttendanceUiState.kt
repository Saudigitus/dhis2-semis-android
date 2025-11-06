package org.saudigitus.semis.attendance.ui

import org.saudigitus.semis.attendance.ui.model.ButtonStep
import org.saudigitus.semis.core.data.model.SearchTeiModel
import org.saudigitus.semis.core.designsystem.attendance.AttendanceButtonState
import org.saudigitus.semis.core.designsystem.components.FilterDetailsState
import org.saudigitus.semis.core.designsystem.components.bottomsheet.model.BottomSheetState
import org.saudigitus.semis.core.designsystem.components.model.ToolbarHeaders

data class AttendanceUiState(
    val isLoading: Boolean = false,
    val hasDataToSave: Boolean = false,
    val displaySummary: Boolean = false,
    val toolbarHeaders: ToolbarHeaders = ToolbarHeaders(""),
    val dateValidator: (Long) -> Boolean = { _ -> true },
    val program: String = "",
    val buttonStep: ButtonStep = ButtonStep.NONE,
    val teis: List<SearchTeiModel> = emptyList(),
    val filterDetailsState: FilterDetailsState = FilterDetailsState(),
    val attendanceButtonState: AttendanceButtonState = AttendanceButtonState(),
    val bottomSheetState: BottomSheetState.SaveState = BottomSheetState.SaveState(),
)
