package org.saudigitus.semis.core.form.ui.state

import org.saudigitus.semis.core.data.model.SearchTeiModel
import org.saudigitus.semis.core.designsystem.attendance.model.AttendanceButtonModel
import org.saudigitus.semis.core.form.data.model.FormType

sealed interface FormEvent {
    data object LoadForm : FormEvent
    data class UpdateAttendance(val tei: SearchTeiModel?, val buttonModel: AttendanceButtonModel) :
        FormEvent

    data class UpdateField(
        val formType: FormType,
        val tei: String,
        val dataElementUid: String,
        val value: String
    ) : FormEvent

    data object SaveEvent : FormEvent

    data class ShowError(val message: String) : FormEvent
    data class ShowSuccess(val message: String) : FormEvent
    data object NavigateBack : FormEvent
}