package org.saudigitus.semis.transfer.components.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.saudigitus.semis.core.data.model.transfer.TransferDecision
import org.saudigitus.semis.core.designsystem.components.chips.OutlinedActionChip
import org.saudigitus.semis.core.designsystem.theme.SemisAccent
import org.saudigitus.semis.core.designsystem.theme.surfaceTone
import org.saudigitus.semis.transfer.R

/**
 * The decision this school owes the school that raised the request. Both actions are
 * per learner on purpose: approving in bulk hides who is being accepted.
 */
@Composable
internal fun TransferDecisionActions(
    processing: Boolean,
    modifier: Modifier = Modifier,
    onDecide: (TransferDecision) -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (processing) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
            )
            return@Row
        }

        OutlinedActionChip(
            label = stringResource(R.string.reject),
            imageVector = Icons.Outlined.ThumbDown,
            contentColor = SemisAccent.Red,
            borderColor = SemisAccent.Red.surfaceTone(alpha = .35f),
            containerColor = SemisAccent.Red.surfaceTone(alpha = .08f),
            onClick = { onDecide(TransferDecision.REJECT) },
        )

        OutlinedActionChip(
            label = stringResource(R.string.approve),
            imageVector = Icons.Outlined.ThumbUp,
            contentColor = SemisAccent.Green,
            borderColor = SemisAccent.Green.surfaceTone(alpha = .35f),
            containerColor = SemisAccent.Green.surfaceTone(alpha = .08f),
            onClick = { onDecide(TransferDecision.APPROVE) },
        )
    }
}
