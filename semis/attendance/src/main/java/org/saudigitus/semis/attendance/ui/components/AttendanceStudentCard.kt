package org.saudigitus.semis.attendance.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.saudigitus.semis.core.data.model.SearchTeiModel
import org.saudigitus.semis.core.data.model.transfer.learnerIdentity
import org.saudigitus.semis.core.designsystem.attendance.AttendanceButtonState
import org.saudigitus.semis.core.designsystem.attendance.model.AttendanceButtonModel
import org.saudigitus.semis.core.designsystem.components.avatar.AvatarInitials
import org.saudigitus.semis.core.designsystem.components.cards.LearnerCard

/**
 * Roster row of the attendance list. Reuses the shared learner card and fills its
 * trailing slot with the status selector, plus the absence reason field underneath when
 * the form asks for it.
 */
@Composable
internal fun AttendanceStudentCard(
    learner: SearchTeiModel,
    attendanceButtonState: AttendanceButtonState,
    modifier: Modifier = Modifier,
    onStatusSelect: (AttendanceButtonModel) -> Unit,
    reasonContent: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val identity = learner.learnerIdentity()
    val teiUid = learner.uid()

    LearnerCard(
        name = identity.name,
        supportingText = identity.firstAttributeValue,
        modifier = modifier,
        avatarColor = AvatarInitials.colorFor(teiUid),
        trailing = {
            AttendanceStatusSelector(
                teiUid = teiUid,
                state = attendanceButtonState,
                onSelect = onStatusSelect,
            )
        },
        supportingContent = reasonContent,
    )
}
