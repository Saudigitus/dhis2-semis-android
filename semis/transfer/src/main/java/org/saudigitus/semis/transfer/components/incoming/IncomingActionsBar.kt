package org.saudigitus.semis.transfer.components.incoming

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.saudigitus.semis.core.data.model.transfer.TransferDecision
import org.saudigitus.semis.core.designsystem.theme.contentTone
import org.saudigitus.semis.core.designsystem.theme.light_error
import org.saudigitus.semis.core.designsystem.theme.light_success
import org.saudigitus.semis.transfer.R

/**
 * Bottom actions of the incoming tab: approve every request at once, or decide the ones
 * picked from the list by long pressing them.
 */
@Composable
internal fun IncomingActionsBar(
    selectedCount: Int,
    enabled: Boolean,
    onApproveAll: () -> Unit,
    onDecideSelected: (TransferDecision) -> Unit,
    onClearSelection: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (selectedCount == 0) {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                enabled = enabled,
                shape = RoundedCornerShape(17.dp),
                onClick = onApproveAll,
            ) {
                Icon(
                    modifier = Modifier.padding(end = 8.dp),
                    imageVector = Icons.Default.DoneAll,
                    contentDescription = null,
                )
                Text(
                    text = stringResource(R.string.approve_all),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DecisionButton(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.approve_selected, selectedCount),
                    tone = light_success,
                    imageVector = Icons.Default.ThumbUp,
                    enabled = enabled,
                    onClick = { onDecideSelected(TransferDecision.APPROVE) },
                )
                DecisionButton(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.reject_selected, selectedCount),
                    tone = light_error,
                    imageVector = Icons.Default.ThumbDown,
                    enabled = enabled,
                    onClick = { onDecideSelected(TransferDecision.REJECT) },
                )
            }
            TextButton(
                modifier = Modifier.align(Alignment.End),
                enabled = enabled,
                onClick = onClearSelection,
            ) {
                Text(text = stringResource(R.string.clear_selection))
            }
        }
    }
}

@Composable
private fun DecisionButton(
    label: String,
    tone: Color,
    imageVector: ImageVector,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    OutlinedButton(
        modifier = modifier.height(52.dp),
        enabled = enabled,
        shape = RoundedCornerShape(17.dp),
        border = BorderStroke(1.dp, tone),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = tone.contentTone()),
        contentPadding = PaddingValues(vertical = 12.dp),
        onClick = onClick,
    ) {
        Icon(
            modifier = Modifier.padding(end = 6.dp),
            imageVector = imageVector,
            contentDescription = null,
        )
        Text(text = label, fontWeight = FontWeight.Bold)
    }
}
