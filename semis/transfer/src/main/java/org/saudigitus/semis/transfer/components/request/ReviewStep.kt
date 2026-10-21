package org.saudigitus.semis.transfer.components.request

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoveDown
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.saudigitus.semis.core.data.model.transfer.learnerIdentity
import org.saudigitus.semis.core.designsystem.components.avatar.AvatarInitials
import org.saudigitus.semis.core.designsystem.components.cards.LearnerCard
import org.saudigitus.semis.core.designsystem.theme.SemisPalette
import org.saudigitus.semis.core.form.data.model.FormFieldState
import org.saudigitus.semis.core.form.ui.state.FormUiState
import org.saudigitus.semis.transfer.R
import org.saudigitus.semis.transfer.components.common.StepIntroduction
import org.saudigitus.semis.transfer.components.destination.CurrentPlacementSummary
import org.saudigitus.semis.core.designsystem.components.cards.DetailSectionCard
import org.saudigitus.semis.transfer.components.common.DetailRows
import org.saudigitus.semis.transfer.state.TransferUiState

/**
 * Third step: who is leaving, where they are now and where they are going, so the
 * request can be checked before it is raised.
 */
@Composable
internal fun ReviewStep(
    state: TransferUiState,
    formState: FormUiState,
    modifier: Modifier = Modifier,
) {
    val destinationRows = buildList {
        add(
            stringResource(R.string.destination_school) to
                state.destinationOrgUnit?.displayName.orEmpty(),
        )
        formState.fields
            .filter {
                it.rendered &&
                    it.dataElementUid != state.statusDataElement &&
                    it.dataElementUid != state.destinationSchoolDataElement &&
                    !it.value.isNullOrBlank()
            }
            .forEach { field -> add(field.label to field.displayValue()) }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            StepIntroduction(
                title = stringResource(R.string.review),
                description = stringResource(R.string.transfer_review),
                horizontalPadding = 0.dp,
            )
        }

        item {
            Text(
                text = stringResource(R.string.selected_records),
                modifier = Modifier.padding(start = 4.dp),
                style = MaterialTheme.typography.labelLarge,
                color = SemisPalette.TextSecondary,
            )
        }

        items(state.selectedRecords, key = { it.tei.uid() }) { record ->
            val identity = record.learnerIdentity()

            LearnerCard(
                name = identity.name,
                modifier = Modifier.fillMaxWidth(),
                supportingText = identity.firstAttributeValue.takeIf { it.isNotBlank() },
                avatarSize = 44.dp,
                avatarColor = AvatarInitials.colorFor(record.tei.uid()),
                containerColor = SemisPalette.CardSurface,
            )
        }

        item {
            DetailSectionCard(
                icon = Icons.Outlined.School,
                title = stringResource(R.string.current_enrollment_section),
                description = stringResource(R.string.current_enrollment_hint),
            ) {
                CurrentPlacementSummary(state.originFilterDetails)
            }
        }

        item {
            DetailSectionCard(
                icon = Icons.Outlined.MoveDown,
                title = stringResource(R.string.destination_school_section),
                description = stringResource(R.string.destination_school_hint),
            ) {
                DetailRows(details = destinationRows)
            }
        }
    }
}

private fun FormFieldState.displayValue(): String =
    selectedOrgUnit?.displayName
        ?: optionSet?.find { it.code == value }?.displayName
        ?: value.orEmpty()
