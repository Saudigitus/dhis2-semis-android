package org.saudigitus.semis.core.form.ui.state

import androidx.compose.runtime.Immutable
import org.saudigitus.semis.core.designsystem.attendance.AttendanceButtonState
import org.saudigitus.semis.core.designsystem.components.model.ToolbarHeaders
import org.saudigitus.semis.core.form.data.model.FormFieldData
import org.saudigitus.semis.core.form.data.model.FormFieldState


@Immutable
data class FormUiState(
    val isLoading: Boolean = false,
    val isEditing: Boolean = false,
    val hasCachedData: Boolean = false,
    val formBuilderState: FormBuilderState = FormBuilderState(),
    val attendanceButtonState: AttendanceButtonState = AttendanceButtonState(),
    val isEnabled: Boolean = false,
    val toolbarHeaders: ToolbarHeaders = ToolbarHeaders(""),
    val fields: List<FormFieldState> = emptyList(),
    val fieldsData: List<FormFieldData> = emptyList(),
    val error: String? = null,
    val isSaved: Boolean = false
)