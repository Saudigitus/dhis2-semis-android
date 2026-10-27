package org.saudigitus.semis.core.designsystem.utils

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

fun Modifier.shimmer(): Modifier = composed {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val transition = rememberInfiniteTransition(label = "shimmer")
    val startOffsetX by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2000f,  // Adjust for wave speed/length
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),  // Duration in ms
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_offset"
    )

    background(
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFf0f0f0),  // Base color
                Color(0xFFe0e0e0),  // Highlight start
                Color(0xFFf0f0f0),  // Peak
                Color(0xFFe0e0e0),  // Fade
                Color(0xFFf0f0f0)   // End
            ),
            start = Offset(startOffsetX - size.width, 0f),
            end = Offset(startOffsetX + size.width, size.height.toFloat())
        ),
        shape = RoundedCornerShape(16.dp)
    ).onGloballyPositioned { coordinates ->
        size = coordinates.size
    }
}