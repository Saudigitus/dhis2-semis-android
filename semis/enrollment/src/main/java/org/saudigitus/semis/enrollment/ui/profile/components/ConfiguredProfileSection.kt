package org.saudigitus.semis.enrollment.ui.profile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.saudigitus.semis.core.data.model.profile.ProfilePanel
import org.saudigitus.semis.core.data.model.profile.ProfileRecord
import org.saudigitus.semis.core.data.model.profile.ProfileTabContent
import org.saudigitus.semis.core.designsystem.components.cards.DetailRows
import org.saudigitus.semis.core.designsystem.components.cards.DetailSectionCard
import org.saudigitus.semis.enrollment.R

/**
 * Draws the tab the deployment configured, one card per panel.
 *
 * The card is the one the transfer steps and the enrollment summary already use, so a record
 * reads the same wherever the app shows it back. Inside, the values keep the grouping the program
 * gives them: a stage groups its data elements into sections and a program groups its attributes
 * the same way, and that grouping is what whoever configured the program chose to say.
 */
@Composable
internal fun ConfiguredProfileSection(
    tab: ProfileTabContent,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        tab.panels.forEach { panel ->
            ConfiguredProfilePanel(panel = panel)
        }
    }
}

@Composable
private fun ConfiguredProfilePanel(
    panel: ProfilePanel,
    modifier: Modifier = Modifier,
) {
    DetailSectionCard(
        icon = if (panel.target == null) Icons.Outlined.Badge else Icons.Outlined.EventNote,
        title = panel.title,
        description = panelDescription(panel),
        modifier = modifier,
    ) {
        if (panel.records.isEmpty()) {
            PanelCaption(text = stringResource(R.string.profile_panel_empty))
            return@DetailSectionCard
        }

        panel.records.forEachIndexed { index, record ->
            // Records of a panel are separate happenings, each with its own date, so the line
            // between them runs the full width of the card. The lines inside a record are inset,
            // which is what tells the reader they are separating values and not records.
            if (index > 0) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }

            ConfiguredProfileRecord(record = record)
        }
    }
}

/** What the panel holds, said in the header so the card is not a title over an unexplained list. */
@Composable
private fun panelDescription(panel: ProfilePanel): String = when (panel.target) {
    // Emptiness is stated once, in the body. Saying it in the header as well would answer the
    // same question twice in the same card.
    null -> stringResource(R.string.profile_panel_attributes)
    else -> pluralStringResource(
        R.plurals.profile_panel_records,
        panel.records.size,
        panel.records.size,
    )
}

@Composable
private fun ConfiguredProfileRecord(
    record: ProfileRecord,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        record.heading?.let { heading -> PanelCaption(text = heading) }

        if (record.sections.isEmpty()) {
            PanelCaption(text = stringResource(R.string.profile_record_empty))
            return@Column
        }

        record.sections.forEachIndexed { index, section ->
            // The rows read as one list whether or not the program groups them: a section adds a
            // heading and the line that introduces it, and nothing else changes. Without that
            // line the list would lose its rhythm exactly where a section holds a single value.
            if (index > 0) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 18.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
                )
            }

            // A record that groups nothing gets its values straight, without a heading naming a
            // grouping the program never made.
            section.title?.takeIf { it.isNotBlank() }?.let { title ->
                Text(
                    modifier = Modifier.padding(horizontal = 18.dp),
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            DetailRows(rows = section.values.map { it.label to it.value })
        }
    }
}

@Composable
private fun PanelCaption(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier.padding(horizontal = 18.dp),
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
