package org.saudigitus.semis.transfer.components.destination

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.School
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.saudigitus.semis.core.form.data.model.FormType
import org.saudigitus.semis.core.form.ui.FormContent
import org.saudigitus.semis.core.form.ui.state.FormEvent
import org.saudigitus.semis.core.form.ui.state.FormUiState
import org.saudigitus.semis.transfer.R
import org.saudigitus.semis.transfer.components.common.StepIntroduction
import org.saudigitus.semis.transfer.state.TransferUiState

@Composable
internal fun DestinationStep(
    state: TransferUiState,
    formState: FormUiState,
    onFormEvent: (FormEvent) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            StepIntroduction(
                title = stringResource(R.string.destination),
                description = stringResource(
                    R.string.destination_description,
                    state.selectedLearnerUids.size,
                ),
                horizontalPadding = 0.dp,
            )
        }
        item {
            DestinationCard(
                icon = Icons.Outlined.School,
                title = stringResource(R.string.origin_details_section),
                description = stringResource(R.string.origin_details_hint),
            ) {
                CurrentPlacementSummary(state.originFilterDetails)
            }
        }
        item {
            DestinationCard(
                icon = Icons.AutoMirrored.Outlined.Assignment,
                title = stringResource(R.string.transfer_details_section),
                description = stringResource(R.string.transfer_details_hint),
            ) {
                FormContent(
                    key = "transfer",
                    type = FormType.DEFAULT,
                    modifier = Modifier.fillMaxWidth(),
                    state = formState,
                    onEvent = onFormEvent,
                )
            }
        }
    }
}
