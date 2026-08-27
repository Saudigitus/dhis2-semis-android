package org.saudigitus.semis.transfer.components.request

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
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
import org.saudigitus.semis.transfer.components.destination.DestinationCard
import org.saudigitus.semis.transfer.state.TransferUiState

/**
 * Second step: the configured transfer form.
 *
 * The status field is filtered out rather than removed: the request still carries the
 * pending code, but there is nothing for the user to decide about it.
 */
@Composable
internal fun DestinationStep(
    state: TransferUiState,
    formState: FormUiState,
    modifier: Modifier = Modifier,
    onFormEvent: (FormEvent) -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            StepIntroduction(
                title = stringResource(R.string.destination),
                description = stringResource(
                    R.string.destination_description,
                    state.selectedRecordUids.size,
                ),
                horizontalPadding = 0.dp,
            )
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
                    fieldFilter = { it.dataElementUid != state.statusDataElement },
                    onEvent = onFormEvent,
                )
            }
        }
    }
}
