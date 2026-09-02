package org.saudigitus.semis.core.designsystem.components.buttons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.saudigitus.semis.core.designsystem.theme.SemisPalette

/**
 * Icon button drawn over a tinted surface. Circular by default; pass a rounded corner
 * [shape] for the squared variant used by the header actions.
 */
@Composable
fun TonalIconButton(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    iconSize: Dp = 18.dp,
    shape: Shape = CircleShape,
    containerColor: Color = SemisPalette.HeaderSurface,
    contentColor: Color = Color.White,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.size(size),
    ) {
        // The tint is painted inside rather than on the button itself: a button of this size
        // is widened to stay comfortable to tap, and a background set on the outside is drawn
        // over that wider area, which makes two of them side by side look like one.
        Box(
            modifier = Modifier
                .size(size)
                .background(color = containerColor, shape = shape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription,
                tint = contentColor,
                modifier = Modifier.size(iconSize),
            )
        }
    }
}
