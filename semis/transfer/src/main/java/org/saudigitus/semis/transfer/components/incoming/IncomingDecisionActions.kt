package org.saudigitus.semis.transfer.components.incoming

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.saudigitus.semis.core.data.model.transfer.TransferDecision
import org.saudigitus.semis.core.designsystem.components.buttons.TonalIconButton
import org.saudigitus.semis.core.designsystem.theme.dark_warning
import org.saudigitus.semis.core.designsystem.theme.light_error
import org.saudigitus.semis.core.designsystem.theme.light_success
import org.saudigitus.semis.core.designsystem.theme.contentTone
import org.saudigitus.semis.core.designsystem.theme.surfaceTone
import org.saudigitus.semis.transfer.R

/**
 * Approve and reject actions offered for a single incoming request.
 */
@Composable
internal fun IncomingDecisionActions(
    processing: Boolean,
    modifier: Modifier = Modifier,
    onDecide: (TransferDecision) -> Unit,
) {
    if (processing) {
        CircularProgressIndicator(
            modifier = modifier.size(24.dp),
            color = dark_warning,
            strokeWidth = 2.dp,
        )
        return
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TonalIconButton(
            imageVector = Icons.Default.ThumbUp,
            contentDescription = stringResource(R.string.approve),
            size = 40.dp,
            iconSize = 19.dp,
            shape = CircleShape,
            containerColor = light_success.surfaceTone(alpha = .22f),
            contentColor = light_success.contentTone(),
            onClick = { onDecide(TransferDecision.APPROVE) },
        )
        TonalIconButton(
            imageVector = Icons.Default.ThumbDown,
            contentDescription = stringResource(R.string.reject),
            size = 40.dp,
            iconSize = 19.dp,
            shape = CircleShape,
            containerColor = light_error.surfaceTone(alpha = .22f),
            contentColor = light_error.contentTone(),
            onClick = { onDecide(TransferDecision.REJECT) },
        )
    }
}
