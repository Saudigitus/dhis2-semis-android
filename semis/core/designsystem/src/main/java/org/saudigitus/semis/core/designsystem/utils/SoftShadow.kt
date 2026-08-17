package org.saudigitus.semis.core.designsystem.utils

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Diffuse elevation shadow used by the card surfaces across the modules.
 */
fun Modifier.softShadow(shape: Shape, elevation: Dp = 10.dp): Modifier = shadow(
    elevation = elevation,
    shape = shape,
    ambientColor = Color.Black.copy(alpha = 0.05f),
    spotColor = Color.Black.copy(alpha = 0.12f),
)
