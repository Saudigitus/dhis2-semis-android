package org.saudigitus.semis.transfer.components.learner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.saudigitus.semis.transfer.R
import org.saudigitus.semis.transfer.components.common.StepIntroduction
import org.saudigitus.semis.transfer.event.TransferUiEvent
import org.saudigitus.semis.transfer.state.TransferUiState

@Composable
internal fun LearnerList(state: TransferUiState, onEvent: (TransferUiEvent) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        StepIntroduction(
            title = stringResource(R.string.select_learners),
            description = stringResource(R.string.select_learners_description),
            badge = state.selectedLearnerUids.size.takeIf { it > 0 }?.let {
                stringResource(R.string.selected_count, it)
            },
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(state.learners, key = { it.tei.uid() }) { learner ->
                LearnerCard(
                    learner = learner,
                    selected = learner.tei.uid() in state.selectedLearnerUids,
                    onClick = { onEvent(TransferUiEvent.ToggleLearner(learner.tei.uid())) },
                )
            }
        }
    }
}
