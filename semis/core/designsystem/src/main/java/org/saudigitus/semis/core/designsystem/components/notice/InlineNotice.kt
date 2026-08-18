package org.saudigitus.semis.core.designsystem.components.notice

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.saudigitus.semis.core.designsystem.R
import org.saudigitus.semis.core.designsystem.theme.borderTone
import org.saudigitus.semis.core.designsystem.theme.surfaceTone

/**
 * Tinted inline banner carrying a short contextual message about the current screen.
 */
@Composable
fun InlineNotice(
    text: String,
    imageVector: ImageVector,
    tone: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = tone.surfaceTone(alpha = .12f),
                shape = RoundedCornerShape(12.dp),
            )
            .border(
                width = 1.dp,
                color = tone.borderTone(alpha = .35f),
                shape = RoundedCornerShape(12.dp),
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = null,
            tint = tone,
            modifier = Modifier.size(18.dp),
        )

        Text(
            text = text,
            color = tone,
            fontSize = 12.5.sp,
            lineHeight = 17.sp,
            fontFamily = FontFamily(Font(R.font.rubik_regular)),
        )
    }
}
