package org.saudigitus.campaign.core.form.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.saudigitus.campaign.core.designsystem.theme.FormSurfaces
import org.saudigitus.campaign.core.form.ui.state.FormStepProgress

/**
 * Shows the steps of a flow as a row of markers, the ones already behind the user ticked off.
 *
 * A bar filling up says how far along someone is but not how much is left, which on a flow that
 * only saves at the end is the thing they need to judge before starting. Counting the markers
 * answers that at a glance.
 */
@Composable
internal fun FormStepIndicator(
    progress: FormStepProgress,
    modifier: Modifier = Modifier,
) {
    if (progress.stepCount <= 0) return

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        for (step in 1..progress.stepCount) {
            if (step > 1) {
                Connector(completed = step <= progress.stepNumber, modifier = Modifier.weight(1f))
            }

            StepMarker(step = step, current = progress.stepNumber)
        }
    }
}

@Composable
private fun StepMarker(step: Int, current: Int) {
    val completed = step < current
    val active = step == current

    Box(
        modifier = Modifier
            .size(28.dp)
            .background(
                color = when {
                    completed || active -> Color.White
                    else -> Color.White.copy(alpha = .24f)
                },
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (completed) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = FormSurfaces.HeaderBlue,
                modifier = Modifier.size(16.dp),
            )
        } else {
            Text(
                text = "$step",
                style = MaterialTheme.typography.labelMedium,
                color = if (active) FormSurfaces.HeaderBlue else Color.White,
            )
        }
    }
}

@Composable
private fun Connector(completed: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(2.dp)
            .background(
                color = if (completed) Color.White else Color.White.copy(alpha = .24f),
                shape = RoundedCornerShape(1.dp),
            ),
    )
}
