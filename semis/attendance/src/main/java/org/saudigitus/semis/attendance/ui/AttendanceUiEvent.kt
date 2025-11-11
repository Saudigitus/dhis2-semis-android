package org.saudigitus.semis.attendance.ui

import org.saudigitus.semis.attendance.ui.model.BottomSheetConfirmAction
import org.saudigitus.semis.attendance.ui.model.BottomSheetType
import org.saudigitus.semis.core.data.model.SearchTeiModel
import org.saudigitus.semis.core.designsystem.attendance.model.AttendanceButtonModel

sealed class AttendanceUiEvent {
    data object NavBack : AttendanceUiEvent()
    data class OnDateSelect(val date: String) : AttendanceUiEvent()
    data object OnSyncClicked : AttendanceUiEvent()
    data object OnEditClicked : AttendanceUiEvent()
    data class BottomSheetAction(val action: BottomSheetConfirmAction) : AttendanceUiEvent()
    data class DismissBottomSheet(val type: BottomSheetType): AttendanceUiEvent()
    data class ShowBottomSheet(val type: BottomSheetType): AttendanceUiEvent()
    data class PerformBulk(val buttonModel: AttendanceButtonModel): AttendanceUiEvent()
    data class OnAttendanceClick(val tei: SearchTeiModel?, val buttonModel: AttendanceButtonModel) :
        AttendanceUiEvent()
}