package org.saudigitus.campaign.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Surface tokens of the form screens. They mirror the palette used by the SEMIS module screens
 * (white cards over a tinted page) without depending on the app design system, so the form stays
 * portable.
 */
object FormSurfaces {
    /** Header blue of the SEMIS design (SemisPalette.HeaderBlue). */
    val HeaderBlue = Color(0xFF1B3F94)
    val ScreenBackground = Color(0xFFEEF2F7)
    val CardSurface = Color(0xFFFFFFFF)
    val FieldSurface = Color(0xFFEEF2F7)
    val SectionTitle = Color(0xFF1B4BA8)
    val TextSecondary = Color(0xFF64748B)

    /** Rounded box every field is drawn in. */
    val FieldShape: Shape = RoundedCornerShape(12.dp)

    /** Rounded top of the content sheet, as on the other module screens. */
    val ScreenShape: Shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
}

/** Soft elevation of the section cards, matching the cards of the module listings. */
fun Modifier.formSoftShadow(shape: Shape, elevation: Dp = 4.dp): Modifier = shadow(
    elevation = elevation,
    shape = shape,
    ambientColor = Color.Black.copy(alpha = 0.05f),
    spotColor = Color.Black.copy(alpha = 0.10f),
)
