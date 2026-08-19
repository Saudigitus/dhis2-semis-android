package org.saudigitus.semis.enrollment.ui.profile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.saudigitus.semis.core.designsystem.components.FilterDetailsState
import org.saudigitus.semis.core.designsystem.components.filters.FilterDetailsInfoCard
import org.saudigitus.semis.core.designsystem.components.header.RoundedBottomHeader
import org.saudigitus.semis.core.designsystem.components.stats.StatTileModel
import org.saudigitus.semis.core.designsystem.components.stats.StatTileRow
import org.saudigitus.semis.core.designsystem.theme.SemisPalette
import org.saudigitus.semis.core.designsystem.R as DesignSystemR

/**
 * Identity band of the learner dashboard: who the learner is, the selection the data is
 * scoped to, and the counters summarising the academic year in view.
 */
@Composable
internal fun StudentProfileHeader(
    name: String,
    systemId: String,
    tiles: List<StatTileModel>,
    filterDetailsState: FilterDetailsState,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
) {
    RoundedBottomHeader(
        modifier = modifier,
        verticalSpacing = 14.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(DesignSystemR.string.back),
                    tint = Color.White,
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = name,
                    color = SemisPalette.OnHeaderPrimary,
                    fontSize = 17.sp,
                    lineHeight = 21.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = FontFamily(Font(DesignSystemR.font.rubik_bold)),
                )
                systemId.takeIf { it.isNotBlank() }?.let { value ->
                    Text(
                        text = value,
                        color = SemisPalette.OnHeaderSecondary,
                        fontSize = 12.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = FontFamily(Font(DesignSystemR.font.rubik_regular)),
                    )
                }
            }
        }

        FilterDetailsInfoCard(state = filterDetailsState)

        StatTileRow(tiles = tiles)
    }
}
