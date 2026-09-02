package org.saudigitus.semis.core.designsystem.components.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItem
import org.saudigitus.semis.core.data.model.SearchTeiModel
import org.saudigitus.semis.core.data.model.transfer.learnerIdentity
import org.saudigitus.semis.core.designsystem.components.avatar.AvatarInitials
import org.saudigitus.semis.core.designsystem.theme.SemisPalette
import org.saudigitus.semis.core.designsystem.theme.semisSoftShadow

/**
 * Tracked entity row shared by the listing screens. Reuses the learner card introduced by the
 * transfer module and lists the attributes mapped for the record under the name.
 */
@Composable
fun TeiLearnerCard(
    tei: SearchTeiModel,
    modifier: Modifier = Modifier,
    additionalInfo: List<AdditionalInfoItem> = emptyList(),
    maxAdditionalInfo: Int = 3,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    supportingContent: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val identity = tei.learnerIdentity()
    val uid = tei.uid()
    val attributes = additionalInfo
        .filter { it.value.isNotBlank() && it.value != identity.firstAttributeValue }
        .take(maxAdditionalInfo)

    LearnerCard(
        name = identity.name,
        supportingText = identity.firstAttributeValue,
        modifier = modifier.semisSoftShadow(CardShape),
        avatarColor = AvatarInitials.colorFor(uid),
        containerColor = SemisPalette.CardSurface,
        shape = CardShape,
        onClick = onClick,
        trailing = trailing,
        supportingContent = if (attributes.isEmpty() && supportingContent == null) {
            null
        } else {
            {
                if (attributes.isNotEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        attributes.forEach { AttributeLine(item = it) }
                    }
                }
                supportingContent?.invoke(this)
            }
        },
    )
}

private val CardShape = RoundedCornerShape(16.dp)

@Composable
private fun AttributeLine(item: AdditionalInfoItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        item.key?.takeIf { it.isNotBlank() }?.let { key ->
            Text(
                text = "${key.trimEnd(':')}:",
                style = MaterialTheme.typography.bodySmall,
                color = SemisPalette.TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = item.value,
            style = MaterialTheme.typography.bodySmall,
            color = item.color ?: SemisPalette.TextSecondary,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
