package org.saudigitus.semis.core.designsystem.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Accents used by the module cards and the icon tiles that go with them, paired with
 * [surfaceTone] for the soft container behind the icon.
 */
object SemisAccent {
    val Blue = Color(0xFF2563EB)
    val Green = Color(0xFF16A34A)
    val Purple = Color(0xFF7C3AED)
    val Amber = Color(0xFFD97706)
    val Teal = Color(0xFF0D9488)
}

/** Shape of the content area right below the toolbar. */
val SemisScreenShape: Shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)

/** Rounded box the form fields are drawn in, as on the enrollment form. */
val SemisFieldShape: Shape = RoundedCornerShape(12.dp)

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
 *
 * The content is clipped to [SemisScreenShape] as well, so headers and lists drawn edge to edge
 * keep the rounded top of the sheet instead of squaring it off.
 */
fun Modifier.semisScreenBackground(): Modifier = fillMaxSize()
    .clip(SemisScreenShape)
    .background(color = SemisPalette.ScreenBackground)
