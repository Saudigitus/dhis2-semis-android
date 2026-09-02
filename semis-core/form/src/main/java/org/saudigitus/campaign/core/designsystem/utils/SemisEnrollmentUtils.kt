package org.saudigitus.campaign.core.designsystem.utils

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import org.saudigitus.campaign.core.designsystem.theme.FormSurfaces

object Utils {
    @Composable
    fun inputColors() = TextFieldDefaults.colors(
        focusedContainerColor = FormSurfaces.FieldSurface,
        unfocusedContainerColor = FormSurfaces.FieldSurface,
        disabledContainerColor = FormSurfaces.FieldSurface,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
        errorIndicatorColor = Color.Transparent,
    )
}

/** Lightweight Compose-only shimmer used by the portable SEMIS form. */
fun Modifier.shimmerEffect(): Modifier = composed {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val transition = rememberInfiniteTransition()
    val offsetX by transition.animateFloat(
        initialValue = -2f * size.width,
        targetValue = 2f * size.width,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = 1_000)),
    )
    // Mirrors the blue palette used by the Campaign loading placeholders.
    val baseColor = Color(0xFF0062A2).copy(alpha = 0.22f)
    val highlightColor = Color(0xFFD1E4FF)

    background(
        brush = Brush.linearGradient(
            colors = listOf(baseColor, highlightColor, baseColor),
            start = Offset(offsetX, 0f),
            end = Offset(offsetX + size.width, size.height.toFloat()),
        ),
    ).onGloballyPositioned { coordinates ->
        size = coordinates.size
    }
}
