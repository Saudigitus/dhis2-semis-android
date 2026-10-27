package org.saudigitus.semis.core.designsystem.components.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Row of [StatTile]s sharing the available width equally. A lone tile keeps its intrinsic
 * width instead of stretching across the row.
 */
@Composable
fun StatTileRow(
    tiles: List<StatTileModel>,
    modifier: Modifier = Modifier,
    spacing: Dp = 8.dp,
) {
    if (tiles.isEmpty()) return

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        tiles.forEach { tile ->
            StatTile(
                model = tile,
                modifier = if (tiles.size > 1) Modifier.weight(1f) else Modifier,
            )
        }
    }
}
