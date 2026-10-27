package org.saudigitus.campaign.core.form.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.saudigitus.campaign.core.form.ui.state.FormStepProgress

/**
 * Title, current step and markers, as every screen of a stepped flow shows them.
 *
 * The steps and the summary that closes them are drawn by different screens, so the header they
 * share lives here: without it the two drift apart and arriving at the end stops feeling like the
 * same flow.
 */
@Composable
fun FormStepHeader(
    title: String,
    stepName: String,
    progress: FormStepProgress,
    modifier: Modifier = Modifier,
    contentColor: Color = Color.White,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.Top),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = contentColor,
        )

        Text(
            text = stepName,
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor,
        )

        FormStepIndicator(progress = progress, modifier = Modifier.padding(top = 6.dp))
    }
}
