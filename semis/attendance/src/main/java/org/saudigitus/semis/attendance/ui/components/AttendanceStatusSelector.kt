package org.saudigitus.semis.attendance.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.saudigitus.semis.attendance.ui.model.selectedStatusId
import org.saudigitus.semis.attendance.ui.model.statusId
import org.saudigitus.semis.attendance.ui.model.toStatusToggleOption
import org.saudigitus.semis.core.designsystem.attendance.AttendanceButtonState
import org.saudigitus.semis.core.designsystem.attendance.model.AttendanceButtonModel
import org.saudigitus.semis.core.designsystem.components.toggles.StatusToggleGroup

/**
 * Status selector for one learner, driven by the configured attendance statuses held in
 * [state]. While the form is read-only it shows the recorded status on its own; taking an
 * attendance expands it into every configured choice.
 */
@Composable
internal fun AttendanceStatusSelector(
    teiUid: String,
    state: AttendanceButtonState,
    modifier: Modifier = Modifier,
    onSelect: (AttendanceButtonModel) -> Unit,
) {
    if (state.buttons.isEmpty()) return

    val options = state.buttons.map { it.toStatusToggleOption(isEditing = state.isEditing) }

    StatusToggleGroup(
        options = options,
        selectedId = selectedStatusId(teiUid, state.attendanceEvents),
        modifier = modifier,
        expanded = state.isEditing,
        onSelect = { option ->
            state.buttons
                .firstOrNull { it.statusId == option.id }
                ?.let(onSelect)
        },
    )
}
