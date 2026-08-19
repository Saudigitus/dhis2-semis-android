package org.saudigitus.semis.core.designsystem.components.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.saudigitus.semis.core.designsystem.R
import org.saudigitus.semis.core.designsystem.theme.SemisAccent
import org.saudigitus.semis.core.designsystem.theme.SemisPalette
import org.saudigitus.semis.core.designsystem.theme.surfaceTone

/**
 * Horizontally scrollable pill tabs. Scrolls rather than squeezing, so a label is never
 * cut to fit more tabs on screen.
 */
@Composable
fun SegmentedTabRow(
    tabs: List<SegmentedTabItem>,
    selectedId: String,
    modifier: Modifier = Modifier,
    accent: Color = SemisAccent.Blue,
    onSelect: (SegmentedTabItem) -> Unit,
) {
    if (tabs.isEmpty()) return

    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        tabs.forEach { tab ->
            val selected = tab.id == selectedId

            Text(
                text = tab.badge?.let { badge -> "${tab.label} ($badge)" } ?: tab.label,
                modifier = Modifier
                    .background(
                        color = if (selected) accent else accent.surfaceTone(alpha = .08f),
                        shape = RoundedCornerShape(100.dp),
                    )
                    .clickable(
                        enabled = !selected,
                        role = Role.Tab,
                        onClick = { onSelect(tab) },
                    )
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                color = if (selected) Color.White else SemisPalette.TextSecondary,
                fontSize = 12.5.sp,
                maxLines = 1,
                fontFamily = FontFamily(
                    Font(if (selected) R.font.rubik_medium else R.font.rubik_regular),
                ),
            )
        }
    }
}
