package org.saudigitus.semis.attendance.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.saudigitus.semis.core.data.model.SearchTeiModel
import org.saudigitus.semis.core.data.model.transfer.learnerIdentity
import org.saudigitus.semis.core.designsystem.attendance.AttendanceButtonState
import org.saudigitus.semis.core.designsystem.attendance.model.AttendanceButtonModel
import org.saudigitus.semis.core.designsystem.components.avatar.AvatarInitials
import org.saudigitus.semis.core.designsystem.components.cards.LearnerCard
import org.saudigitus.semis.core.designsystem.utils.softShadow

/**
 * Roster row of the attendance list. Reuses the shared learner card and fills its
 * trailing slot with the status selector, plus the absence reason field underneath when
 * the form asks for it.
 *
 * [showStatusSelector] is false until the day carries an attendance status, so the
 * learners are listed without a status to read while none has been recorded.
 */
@Composable
internal fun AttendanceStudentCard(
    learner: SearchTeiModel,
    attendanceButtonState: AttendanceButtonState,
    modifier: Modifier = Modifier,
    showStatusSelector: Boolean = true,
    onStatusSelect: (AttendanceButtonModel) -> Unit,
    reasonContent: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val identity = learner.learnerIdentity()
    val teiUid = learner.uid()

    LearnerCard(
        name = identity.name,
        supportingText = identity.firstAttributeValue,
        modifier = modifier
            .softShadow(RoundedCornerShape(20.dp), 15.dp),
        avatarColor = AvatarInitials.colorFor(teiUid),
        containerColor = Color.White,
        trailing = if (showStatusSelector) {
            {
                AttendanceStatusSelector(
                    teiUid = teiUid,
                    state = attendanceButtonState,
                    onSelect = onStatusSelect,
                )
            }
        } else {
            null
        },
        supportingContent = reasonContent,
    )
}
