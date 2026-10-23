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
    data object ResetForm : AttendanceUiEvent()
    data object ConfirmResetForm : AttendanceUiEvent()
    data object DismissResetDialog : AttendanceUiEvent()
    data object BulkOverrideAttendance : AttendanceUiEvent()

    /** Cancel on the save confirmation: nothing is saved and the changes stay on screen. */
    data object DismissSaveDialog : AttendanceUiEvent()

    /** Cancel on overriding the register with one status for the class: the marks stay. */
    data object DismissBulkOverride : AttendanceUiEvent()
    data class BottomSheetAction(val action: BottomSheetConfirmAction) : AttendanceUiEvent()
    data class DismissBottomSheet(val type: BottomSheetType): AttendanceUiEvent()
    data class ShowBottomSheet(val type: BottomSheetType): AttendanceUiEvent()
    data class PerformBulk(val buttonModel: AttendanceButtonModel): AttendanceUiEvent()
    data class OnAttendanceClick(val tei: SearchTeiModel?, val buttonModel: AttendanceButtonModel) :
        AttendanceUiEvent()

    data class AddAttendanceStatus(val status: Boolean): AttendanceUiEvent()
}
