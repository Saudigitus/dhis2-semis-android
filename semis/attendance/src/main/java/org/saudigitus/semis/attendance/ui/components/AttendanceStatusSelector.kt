package org.saudigitus.semis.attendance.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import org.saudigitus.semis.attendance.ui.model.statusId
import org.saudigitus.semis.attendance.ui.model.selectedStatusId
import org.saudigitus.semis.attendance.ui.model.toLetterToggleOption
import org.saudigitus.semis.core.designsystem.attendance.AttendanceButtonState
import org.saudigitus.semis.core.designsystem.attendance.model.AttendanceButtonModel
import org.saudigitus.semis.core.designsystem.components.toggles.LetterToggleGroup

/**
 * Single-letter status selector for one learner, driven by the configured attendance
 * statuses held in [state].
 */
@Composable
internal fun AttendanceStatusSelector(
    teiUid: String,
    state: AttendanceButtonState,
    modifier: Modifier = Modifier,
    onSelect: (AttendanceButtonModel) -> Unit,
) {
    if (state.buttons.isEmpty()) return

    val options = remember(state.buttons, state.isEditing) {
        state.buttons.map { it.toLetterToggleOption(isEditing = state.isEditing) }
    }

    LetterToggleGroup(
        options = options,
        selectedId = selectedStatusId(teiUid, state.attendanceEvents),
        modifier = modifier,
        onSelect = { option ->
            state.buttons
                .firstOrNull { it.statusId == option.id }
                ?.let(onSelect)
        },
    )
}
