package org.saudigitus.semis.enrollment.ui.profile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Diversity3
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.saudigitus.semis.core.data.model.profile.SocioEconomicRecord
import org.saudigitus.semis.core.data.model.profile.TeiProfile
import org.saudigitus.semis.core.designsystem.components.cards.DetailCard
import org.saudigitus.semis.core.designsystem.components.pills.StatusPill
import org.saudigitus.semis.core.designsystem.components.rows.LabelValueRow
import org.saudigitus.semis.core.designsystem.theme.SemisAccent
import org.saudigitus.semis.core.designsystem.theme.contentTone
import org.saudigitus.semis.core.designsystem.theme.light_success
import org.saudigitus.semis.core.designsystem.theme.surfaceTone
import org.saudigitus.semis.enrollment.R
import org.saudigitus.semis.core.utils.DateHelper

/**
 * Identity attributes of the learner followed by the socio-economic records captured for
 * them, mirroring the two panels of the web profile.
 */
@Composable
internal fun StudentDetailsSection(
    profile: TeiProfile,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        DetailCard(
            title = stringResource(R.string.profile_identity),
            imageVector = Icons.Outlined.Badge,
            accent = SemisAccent.Blue,
        ) {
            if (profile.identity.isEmpty()) {
                ProfileEmptyLine(text = stringResource(R.string.profile_no_identity))
            } else {
                profile.identity.forEach { attribute ->
                    LabelValueRow(label = attribute.label, value = attribute.value)
                }
            }
        }

        DetailCard(
            title = stringResource(R.string.profile_socio_economics),
            imageVector = Icons.Outlined.Diversity3,
            accent = SemisAccent.Purple,
        ) {
            if (profile.socioEconomics.isEmpty()) {
                ProfileEmptyLine(text = stringResource(R.string.profile_no_socio_economics))
            } else {
                profile.socioEconomics.forEach { record ->
                    SocioEconomicBlock(record = record)
                }
            }
        }
    }
}

@Composable
private fun SocioEconomicBlock(record: SocioEconomicRecord) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LabelValueRow(
            label = stringResource(R.string.profile_occurred_at),
            value = record.occurredAt?.let { DateHelper.formatDate(it.time) },
        )
        LabelValueRow(
            label = stringResource(R.string.profile_school),
            value = record.orgUnitName,
        )

        if (record.isActive) {
            StatusPill(
                text = stringResource(R.string.profile_active),
                containerColor = light_success.surfaceTone(alpha = .18f),
                contentColor = light_success.contentTone(),
            )
        }

        record.details.forEach { detail ->
            LabelValueRow(label = detail.label, value = detail.value)
        }
    }
}
