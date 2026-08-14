package org.saudigitus.semis.transfer.components.navigation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.saudigitus.semis.transfer.R
import org.saudigitus.semis.transfer.model.TransferStep

@Composable
internal fun TransferProgress(step: TransferStep) {
    val current = step.ordinal
    val progress by animateFloatAsState(
        targetValue = (current + 1) / TransferStep.entries.size.toFloat(),
        animationSpec = tween(250),
        label = "step_progress",
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                stringResource(R.string.transfer_progress),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "${current + 1} / ${TransferStep.entries.size}",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            TransferStep.entries.forEachIndexed { index, item ->
                val active = index <= current
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (index == current) 30.dp else 26.dp)
                            .background(
                                if (active) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant,
                                CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (index < current) {
                            Icon(
                                modifier = Modifier.size(15.dp),
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                            )
                        } else {
                            Text(
                                "${index + 1}",
                                color = if (active) Color.White
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                    Text(
                        stringResource(item.shortLabel()),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (index == current) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (index == current) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

private fun TransferStep.shortLabel(): Int = when (this) {
    TransferStep.SELECT_LEARNERS -> R.string.step_learners_short
    TransferStep.DESTINATION -> R.string.step_destination_short
    TransferStep.REVIEW -> R.string.step_review_short
}
