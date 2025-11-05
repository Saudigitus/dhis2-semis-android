package org.saudigitus.semis.attendance.ui

import org.saudigitus.semis.core.data.model.SearchTeiModel
import org.saudigitus.semis.core.designsystem.attendance.model.AttendanceButtonModel

sealed class AttendanceUiEvent {
    data object NavBack : AttendanceUiEvent()
    data class OnDateSelect(val date: String) : AttendanceUiEvent()
    data object OnSyncClicked : AttendanceUiEvent()
    data object OnEditClicked : AttendanceUiEvent()
    data object OnSaveClicked : AttendanceUiEvent()
    data object DismissBottomSheet: AttendanceUiEvent()
    data object ShowBottomSheet: AttendanceUiEvent()
    data class OnAttendanceClick(val tei: SearchTeiModel?, val buttonModel: AttendanceButtonModel) :
        AttendanceUiEvent()
}