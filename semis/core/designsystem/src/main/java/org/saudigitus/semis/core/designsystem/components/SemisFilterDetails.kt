package org.saudigitus.semis.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.saudigitus.semis.core.designsystem.theme.SemisAccent
import org.saudigitus.semis.core.designsystem.theme.SemisPalette
import org.saudigitus.semis.core.designsystem.theme.semisSoftShadow
import org.saudigitus.semis.core.designsystem.theme.surfaceTone

/**
 * Selection card shown at the top of the module screens: the grade and section being worked on,
 * the academic year and school underneath, and how many learners the selection holds.
 *
 * Restyled variant of [FilterDetails], introduced by the home screen and shared with the module
 * listings so every screen opens with the same header.
 */
@Composable
fun SemisFilterDetails(
    modifier: Modifier = Modifier,
    state: FilterDetailsState,
    accent: Color = SemisAccent.Blue,
    showChevron: Boolean = true,
    onClick: () -> Unit = {},
) {
    val title = listOfNotNull(state.grade, state.section)
        .filter { it.isNotEmpty() }
        .joinToString(separator = " · ")
        .ifEmpty { state.orgUnit }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semisSoftShadow(CardShape),
        onClick = onClick,
        enabled = state.enable,
        shape = CardShape,
        colors = CardDefaults.cardColors(
            containerColor = SemisPalette.CardSurface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContainerColor = SemisPalette.CardSurface,
            disabledContentColor = MaterialTheme.colorScheme.onSurface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = RoundedCornerShape(14.dp),
                color = accent.surfaceTone(0.12f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        modifier = Modifier.size(22.dp),
                        imageVector = Icons.Rounded.School,
                        contentDescription = null,
                        tint = accent,
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${state.academicYear} · ${state.orgUnit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (state.enableCounter) {
                Surface(
                    shape = CircleShape,
                    color = accent.surfaceTone(0.12f),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            modifier = Modifier.size(15.dp),
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null,
                            tint = accent,
                        )
                        Text(
                            text = "${state.count}",
                            style = MaterialTheme.typography.labelLarge,
                            color = accent,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            if (state.enable && showChevron) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private val CardShape = RoundedCornerShape(20.dp)
