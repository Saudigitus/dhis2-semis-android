package org.saudigitus.semis.core.designsystem.components.filters

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.saudigitus.semis.core.designsystem.R
import org.saudigitus.semis.core.designsystem.theme.SemisPalette
import org.saudigitus.semis.core.designsystem.utils.softShadow

/**
 * Card summarizing the filter context a screen is scoped to. Entries without a value are
 * left out, so optional levels such as grade or class only appear once selected.
 */
@Composable
fun FilterInfoCard(
    items: List<FilterInfoItem>,
    modifier: Modifier = Modifier,
    columns: Int = 2,
    accentColor: Color = SemisPalette.ActionBlue,
    containerColor: Color = SemisPalette.CardSurface,
    iconContainerColor: Color = accentColor.copy(alpha = .10f),
    labelColor: Color = SemisPalette.TextMuted,
    valueColor: Color = SemisPalette.TextPrimary,
    elevation: Dp = 4.dp,
) {
    val visibleItems = items.filter { it.value.isNotBlank() }

    if (visibleItems.isEmpty()) return

    val shape = RoundedCornerShape(14.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .softShadow(shape, elevation)
            .background(color = containerColor, shape = shape)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        visibleItems.chunked(columns).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                row.forEach { item ->
                    FilterInfoEntry(
                        item = item,
                        accentColor = accentColor,
                        iconContainerColor = iconContainerColor,
                        labelColor = labelColor,
                        valueColor = valueColor,
                        modifier = Modifier.weight(1f),
                    )
                }

                repeat(columns - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun FilterInfoEntry(
    item: FilterInfoItem,
    accentColor: Color,
    iconContainerColor: Color,
    labelColor: Color,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(color = iconContainerColor, shape = CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(15.dp),
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                text = item.label,
                color = labelColor,
                fontSize = 9.5.sp,
                lineHeight = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontFamily = FontFamily(Font(R.font.rubik_regular)),
            )
            Text(
                text = item.value,
                color = valueColor,
                fontSize = 12.5.sp,
                lineHeight = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontFamily = FontFamily(Font(R.font.rubik_medium)),
            )
        }
    }
}
