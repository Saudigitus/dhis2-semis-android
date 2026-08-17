package org.saudigitus.semis.transfer.components.learner

import androidx.compose.runtime.Composable
import org.saudigitus.semis.core.data.model.SearchTeiModel
import org.saudigitus.semis.core.data.model.transfer.learnerIdentity
import org.saudigitus.semis.core.designsystem.components.cards.LearnerCard
import org.saudigitus.semis.core.designsystem.components.cards.SelectionCheckIndicator

@Composable
internal fun LearnerCard(learner: SearchTeiModel, selected: Boolean, onClick: () -> Unit) {
    val identity = learner.learnerIdentity()

    LearnerCard(
        name = identity.name,
        supportingText = identity.firstAttributeValue,
        selected = selected,
        onClick = onClick,
        trailing = { SelectionCheckIndicator(selected = selected) },
    )
}
