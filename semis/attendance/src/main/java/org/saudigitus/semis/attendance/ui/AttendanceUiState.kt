package org.saudigitus.semis.attendance.ui

import androidx.compose.runtime.Immutable
import org.saudigitus.semis.core.designsystem.components.summary.SummaryState
import org.saudigitus.semis.attendance.ui.model.ButtonStep
import org.saudigitus.semis.core.data.model.SearchTeiModel
import org.saudigitus.semis.core.designsystem.attendance.model.AttendanceButtonModel
import org.saudigitus.semis.core.designsystem.components.bottomsheet.model.BottomSheetState
import org.saudigitus.semis.core.designsystem.components.model.ToolbarHeaders
import org.saudigitus.semis.core.form.ui.state.FormBuilderState

@Immutable
data class AttendanceUiState(
    val isLoading: Boolean = false,
    val displayBulk: Boolean = false,
    val toolbarHeaders: ToolbarHeaders = ToolbarHeaders(""),
    val dateValidator: (Long) -> Boolean = { _ -> true },
    val program: String = "",
    val buttonStep: ButtonStep = ButtonStep.NONE,
    val teis: List<SearchTeiModel> = emptyList(),
    val formBuilderState: FormBuilderState = FormBuilderState(),
    val attendanceSummaryState: SummaryState = SummaryState(),
    val bottomSheetState: BottomSheetState.HasItemsState = BottomSheetState.HasItemsState(),
    val genericsBottomSheetState: BottomSheetState.GenericsState<AttendanceButtonModel> = BottomSheetState.GenericsState(),
    val errorMessage: String? = null
)
