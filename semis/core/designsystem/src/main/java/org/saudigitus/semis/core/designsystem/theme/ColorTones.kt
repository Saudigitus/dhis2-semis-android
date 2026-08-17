package org.saudigitus.semis.core.designsystem.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/**
 * Derives the soft container tint used by chips and toggles from a status color.
 */
fun Color.surfaceTone(alpha: Float = 0.14f): Color = copy(alpha = alpha)

/**
 * Derives a readable foreground from a status color by darkening it towards black,
 * so lightly configured colors keep enough contrast on a white surface.
 */
fun Color.contentTone(factor: Float = 0.4f): Color = lerp(this, Color.Black, factor)

/**
 * Derives the hairline border used by chips and toggles from a status color.
 */
fun Color.borderTone(alpha: Float = 0.45f): Color = copy(alpha = alpha)
