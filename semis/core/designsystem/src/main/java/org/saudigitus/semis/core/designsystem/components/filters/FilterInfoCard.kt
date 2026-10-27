package org.saudigitus.semis.core.designsystem.components.filters

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.saudigitus.semis.core.designsystem.R
import org.saudigitus.semis.core.designsystem.theme.SemisPalette
import org.saudigitus.semis.core.designsystem.utils.softShadow

/**
 * Card summarizing the filter context a screen is scoped to, laid out as a single row of
 * entries sharing the width equally. Entries without a value are left out, so optional
 * levels such as grade or class only appear once selected and the remaining ones widen.
 */
@Composable
fun FilterInfoCard(
    items: List<FilterInfoItem>,
    modifier: Modifier = Modifier,
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

    Row(
        modifier = modifier
            .fillMaxWidth()
            .softShadow(shape, elevation)
            .background(color = containerColor, shape = shape)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Top,
    ) {
        visibleItems.forEach { item ->
            FilterInfoEntry(
                item = item,
                accentColor = accentColor,
                iconContainerColor = iconContainerColor,
                labelColor = labelColor,
                valueColor = valueColor,
                modifier = Modifier.weight(1f),
            )
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
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(3.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(color = iconContainerColor, shape = CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(13.dp),
            )
        }

        Text(
            text = item.label,
            color = labelColor,
            fontSize = 9.sp,
            lineHeight = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            fontFamily = FontFamily(Font(R.font.rubik_regular)),
        )
        Text(
            text = item.value,
            color = valueColor,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            fontFamily = FontFamily(Font(R.font.rubik_medium)),
        )
    }
}
