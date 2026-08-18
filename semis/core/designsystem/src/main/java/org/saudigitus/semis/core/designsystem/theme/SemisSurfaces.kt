package org.saudigitus.semis.core.designsystem.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Shape of the content area right below the toolbar. */
val SemisScreenShape: Shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)

/** Soft elevation used by the SEMIS cards, mirroring the transfer module look. */
fun Modifier.semisSoftShadow(shape: Shape, elevation: Dp = 4.dp): Modifier = shadow(
    elevation = elevation,
    shape = shape,
    ambientColor = Color.Black.copy(alpha = 0.05f),
    spotColor = Color.Black.copy(alpha = 0.10f),
)

/**
 * Paints [SemisPalette.ScreenBackground] over the scaffold content area, so the white cards
 * read as cards instead of blending with the page.
 */
fun Modifier.semisScreenBackground(): Modifier = fillMaxSize()
    .background(color = SemisPalette.ScreenBackground, shape = SemisScreenShape)
