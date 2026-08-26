package org.saudigitus.semis.enrollment.ui.form.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.saudigitus.semis.core.designsystem.theme.SemisAccent
import org.saudigitus.semis.core.designsystem.theme.SemisPalette
import org.saudigitus.semis.enrollment.R

/**
 * Confirms the enrollment and reports the identifiers the learner is now known by.
 *
 * The flow ends here rather than dropping the user straight back on the list, because the numbers
 * the server minted are only useful if they are read once, and this is the only moment they are
 * shown together.
 *
 * @param identifiers label and value of each generated identifier.
 * @param onRegisterAnother starts another enrollment without leaving the flow.
 * @param onDone returns to wherever the enrollment was started from.
 */
@Composable
internal fun EnrollmentCompletedScreen(
    identifiers: List<Pair<String, String>>,
    onRegisterAnother: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SemisPalette.ScreenBackground)
            .systemBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = SemisAccent.Green,
            modifier = Modifier.size(64.dp),
        )

        Text(
            text = stringResource(R.string.enrollment_complete_title),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )

        Text(
            text = stringResource(R.string.enrollment_complete_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )

        identifiers.forEach { (label, value) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SemisPalette.CardSurface),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(text = label, style = MaterialTheme.typography.bodySmall)
                    Text(text = value, style = MaterialTheme.typography.titleMedium)
                }
            }
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onDone,
        ) {
            Text(text = stringResource(R.string.enrollment_complete_done))
        }

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onRegisterAnother,
        ) {
            Text(text = stringResource(R.string.enrollment_register_another))
        }
    }
}
