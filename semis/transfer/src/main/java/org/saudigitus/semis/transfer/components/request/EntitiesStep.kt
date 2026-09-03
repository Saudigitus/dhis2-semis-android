package org.saudigitus.semis.transfer.components.request

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.saudigitus.semis.core.data.model.SearchTeiModel
import org.saudigitus.semis.core.data.model.transfer.learnerIdentity
import org.saudigitus.semis.core.designsystem.components.FilterDetailsState
import org.saudigitus.semis.core.designsystem.components.NoResults
import org.saudigitus.semis.core.designsystem.components.SemisFilterDetails
import org.saudigitus.semis.core.designsystem.components.avatar.AvatarInitials
import org.saudigitus.semis.core.designsystem.components.cards.LearnerCard
import org.saudigitus.semis.core.designsystem.components.cards.SelectionCheckIndicator
import org.saudigitus.semis.core.designsystem.theme.SemisPalette
import org.saudigitus.semis.transfer.R
import org.saudigitus.semis.transfer.components.common.StepIntroduction

/**
 * First step: which enrollments are being sent.
 *
 * The listing follows the filters chosen on the home screen, so the class those filters
 * name is stated above it rather than left for the user to remember.
 *
 * Records already awaiting a decision are not offered, since one can only be part of a
 * single request at a time.
 */
@Composable
internal fun EntitiesStep(
    placement: FilterDetailsState,
    records: List<SearchTeiModel>,
    selectedUids: Set<String>,
    modifier: Modifier = Modifier,
    onToggle: (String) -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            // The card counts what can be picked, not the whole class: records
            // already awaiting a decision are not on the list below it.
            SemisFilterDetails(
                modifier = Modifier.fillMaxWidth(),
                state = placement.copy(count = records.size),
                showChevron = false,
            )
        }

        item {
            StepIntroduction(
                title = null,
                description = stringResource(R.string.step_entities_description),
                horizontalPadding = 0.dp,
                badge = selectedUids.size
                    .takeIf { it > 0 }
                    ?.let { stringResource(R.string.selected_count, it) },
            )
        }

        if (records.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    NoResults(message = stringResource(R.string.no_records_to_transfer))
                }
            }
            return@LazyColumn
        }

        items(records, key = { it.tei.uid() }) { record ->
            val identity = record.learnerIdentity()
            val uid = record.tei.uid()
            val selected = uid in selectedUids

            LearnerCard(
                name = identity.name,
                modifier = Modifier.fillMaxWidth(),
                supportingText = identity.firstAttributeValue.takeIf { it.isNotBlank() },
                selected = selected,
                avatarColor = AvatarInitials.colorFor(uid),
                containerColor = SemisPalette.CardSurface,
                onClick = { onToggle(uid) },
                trailing = { SelectionCheckIndicator(selected = selected) },
            )
        }
    }
}
