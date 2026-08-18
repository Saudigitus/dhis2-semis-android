package org.saudigitus.semis.attendance.ui.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import org.saudigitus.semis.core.designsystem.attendance.model.AttendanceButtonModel
import org.saudigitus.semis.core.designsystem.attendance.model.AttendanceEventWithDecorator
import org.saudigitus.semis.core.designsystem.components.toggles.StatusToggleOption
import org.saudigitus.semis.core.designsystem.theme.borderTone
import org.saudigitus.semis.core.designsystem.theme.contentTone
import org.saudigitus.semis.core.designsystem.theme.surfaceTone
import org.saudigitus.semis.core.designsystem.utils.UiDefaults

/**
 * Identifies the configured status that carries no code, which the form treats as the
 * implicit "present" default when a learner has no attendance event.
 */
internal const val DEFAULT_STATUS_ID = "__default_status__"

internal val AttendanceButtonModel.statusId: String
    get() = code ?: DEFAULT_STATUS_ID

/**
 * Renders a configured attendance status as an icon toggle, keeping the icon and the
 * color coming from the app configuration.
 */
@Composable
internal fun AttendanceButtonModel.toStatusToggleOption(
    isEditing: Boolean,
): StatusToggleOption {
    val statusColor = color ?: UiDefaults.getAttendanceStatusColor(key)
    val statusLabel = name?.takeIf { it.isNotBlank() } ?: key

    return StatusToggleOption(
        id = statusId,
        icon = icon ?: ImageVector.vectorResource(
            UiDefaults.getIconByName(iconName.orEmpty()),
        ),
        label = statusLabel,
        selectedContainerColor = statusColor,
        containerColor = statusColor.surfaceTone(),
        contentColor = statusColor.contentTone(),
        borderColor = statusColor.borderTone(),
        enabled = enabled && isEditing,
    )
}

/**
 * Status currently recorded for [teiUid], mirroring the form rule that an absent event
 * means the code-less default status is the selected one.
 */
internal fun selectedStatusId(
    teiUid: String,
    attendanceEvents: List<AttendanceEventWithDecorator>,
): String {
    val event = attendanceEvents.find { it.event?.tei == teiUid }

    return event?.event?.value?.takeIf { it.isNotBlank() } ?: DEFAULT_STATUS_ID
}
