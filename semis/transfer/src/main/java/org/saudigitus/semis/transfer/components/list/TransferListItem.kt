package org.saudigitus.semis.transfer.components.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.saudigitus.semis.core.data.model.transfer.TeiTransfer
import org.saudigitus.semis.core.data.model.transfer.TransferDecision
import org.saudigitus.semis.core.designsystem.components.avatar.AvatarInitials
import org.saudigitus.semis.core.designsystem.components.cards.LearnerCard
import org.saudigitus.semis.core.designsystem.components.pills.StatusPill
import org.saudigitus.semis.core.designsystem.theme.SemisPalette
import org.saudigitus.semis.core.designsystem.theme.surfaceTone
import org.saudigitus.semis.transfer.R
import org.saudigitus.semis.transfer.components.common.DetailRows
import org.saudigitus.semis.transfer.model.RelativeTimeUnit
import org.saudigitus.semis.transfer.model.TransferTab
import org.saudigitus.semis.transfer.model.relativeTime
import java.util.Date

/**
 * One request in either list: the learner as the enrollment list shows them, the status
 * on the right in the colour of its state, and how long ago the request was raised.
 *
 * The other school, the class and the reason are stated under the name because they are
 * what a school weighs when deciding, and opening the request to find them would cost a
 * screen. Which school that is depends on the side being read: the destination on the
 * way out, the origin on the way in.
 *
 * [onDecide] is only supplied on the incoming tab, where this school is the one that
 * has to answer.
 */
@Composable
internal fun TransferListItem(
    transfer: TeiTransfer,
    tab: TransferTab,
    now: Date,
    modifier: Modifier = Modifier,
    processing: Boolean = false,
    onDecide: ((TransferDecision) -> Unit)? = null,
) {
    val accent = transfer.status.accentColor
    val showsDecision = onDecide != null && transfer.isPending

    LearnerCard(
        name = transfer.recordName,
        modifier = modifier.fillMaxWidth(),
        supportingText = transfer.firstAttributeValue.takeIf { it.isNotBlank() },
        avatarColor = AvatarInitials.colorFor(transfer.teiUid),
        containerColor = SemisPalette.CardSurface,
        trailing = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                StatusPill(
                    text = stringResource(transfer.status.label),
                    containerColor = accent.surfaceTone(alpha = .12f),
                    contentColor = accent,
                )

                RelativeTimeLabel(from = transfer.requestedAt, now = now)
            }
        },
        supportingContent = {
            val counterpartLabel = when (tab) {
                TransferTab.OUTGOING -> stringResource(R.string.transfer_destination_school)
                TransferTab.INCOMING -> stringResource(R.string.transfer_origin_school)
            }
            val counterpart = when (tab) {
                TransferTab.OUTGOING ->
                    transfer.destinationSchoolName.ifBlank { transfer.destinationOrgUnit }

                TransferTab.INCOMING ->
                    transfer.originSchoolName.ifBlank { transfer.originOrgUnit }
            }

            DetailRows(
                details = listOf(
                    counterpartLabel to
                        counterpart.ifBlank {
                            stringResource(R.string.transfer_detail_missing)
                        },
                    stringResource(R.string.transfer_grade) to
                        transfer.grade.ifBlank {
                            stringResource(R.string.transfer_detail_missing)
                        },
                    stringResource(R.string.transfer_reason) to
                        transfer.reason.ifBlank {
                            stringResource(R.string.transfer_detail_missing)
                        },
                ),
                modifier = Modifier.padding(top = 6.dp),
                horizontalPadding = 0.dp,
                verticalPadding = 8.dp,
                leadingDivider = true,
            )

            if (showsDecision) {
                TransferDecisionActions(
                    processing = processing,
                    modifier = Modifier.padding(top = 8.dp),
                    onDecide = { decision -> onDecide?.invoke(decision) },
                )
            }
        },
    )
}

@Composable
private fun RelativeTimeLabel(from: Date, now: Date, modifier: Modifier = Modifier) {
    val elapsed = relativeTime(from = from, now = now)
    val text = if (elapsed.unit == RelativeTimeUnit.JUST_NOW) {
        stringResource(elapsed.unit.pluralLabel)
    } else {
        pluralStringResource(elapsed.unit.pluralLabel, elapsed.amount, elapsed.amount)
    }

    Text(
        text = text,
        modifier = modifier,
        color = SemisPalette.TextSecondary,
        fontSize = 11.sp,
        textAlign = TextAlign.End,
        maxLines = 1,
    )
}
