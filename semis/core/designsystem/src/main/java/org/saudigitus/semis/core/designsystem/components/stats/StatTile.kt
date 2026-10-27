package org.saudigitus.semis.core.designsystem.components.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.saudigitus.semis.core.designsystem.R

/**
 * Colored tile showing a count above its label, used by summary tile rows.
 */
@Composable
fun StatTile(
    model: StatTileModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(
                color = model.containerColor,
                shape = RoundedCornerShape(12.dp),
            )
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "${model.value}",
            color = model.contentColor,
            fontSize = 19.sp,
            lineHeight = 22.sp,
            maxLines = 1,
            fontFamily = FontFamily(Font(R.font.rubik_bold)),
        )
        Text(
            text = model.label,
            color = model.contentColor.copy(alpha = .75f),
            fontSize = 10.5.sp,
            lineHeight = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            fontFamily = FontFamily(Font(R.font.rubik_regular)),
        )
    }
}
