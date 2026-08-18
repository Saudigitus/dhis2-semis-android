package org.saudigitus.semis.transfer.components.outgoing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.saudigitus.semis.core.data.model.transfer.OutgoingTeiTransfer
import org.saudigitus.semis.core.designsystem.components.cards.LearnerCard
import org.saudigitus.semis.transfer.R
import org.saudigitus.semis.transfer.components.common.StatusPill

/**
 * A learner whose transfer request is waiting for the destination school to approve it.
 */
@Composable
internal fun PendingOutgoingStudentCard(transfer: OutgoingTeiTransfer) {
    LearnerCard(
        name = transfer.learnerName,
        supportingText = transfer.firstAttributeValue,
        trailing = { StatusPill(stringResource(R.string.pending)) },
        supportingContent = {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    modifier = Modifier.size(38.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            modifier = Modifier.size(20.dp),
                            imageVector = Icons.Outlined.School,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }
                Column1(transfer)
            }
        },
    )
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.Column1(transfer: OutgoingTeiTransfer) {
    androidx.compose.foundation.layout.Column(modifier = Modifier.weight(1f)) {
        Text(
            stringResource(R.string.to_school),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            transfer.destinationSchoolName.ifBlank { transfer.destinationOrgUnit },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
