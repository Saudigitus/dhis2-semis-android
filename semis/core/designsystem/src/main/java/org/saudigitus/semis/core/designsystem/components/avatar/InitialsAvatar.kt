package org.saudigitus.semis.core.designsystem.components.avatar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.saudigitus.semis.core.designsystem.R

/**
 * Circular avatar showing the initials of [name] over a color derived from [colorKey].
 */
@Composable
fun InitialsAvatar(
    name: String,
    modifier: Modifier = Modifier,
    colorKey: String = name,
    size: Dp = 42.dp,
    fontSize: TextUnit = 13.sp,
    containerColor: Color = AvatarInitials.colorFor(colorKey),
    contentColor: Color = Color.White,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(color = containerColor, shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = AvatarInitials.of(name),
            color = contentColor,
            fontSize = fontSize,
            maxLines = 1,
            fontFamily = FontFamily(Font(R.font.rubik_bold)),
        )
    }
}
