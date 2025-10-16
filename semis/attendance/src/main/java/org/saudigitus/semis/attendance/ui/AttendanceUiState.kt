package org.saudigitus.semis.attendance.ui

import org.saudigitus.semis.core.data.model.SearchTeiModel
import org.saudigitus.semis.core.designsystem.attendance.AttendanceButtonState
import org.saudigitus.semis.core.designsystem.components.FilterDetailsState
import org.saudigitus.semis.core.designsystem.components.model.ToolbarHeaders

data class AttendanceUiState(
    val isLoading: Boolean = false,
    val toolbarHeaders: ToolbarHeaders = ToolbarHeaders(""),
    val program: String = "",
    val teis: List<SearchTeiModel> = emptyList(),
    val filterDetailsState: FilterDetailsState = FilterDetailsState(),
    val attendanceButtonState: AttendanceButtonState = AttendanceButtonState(),
)
