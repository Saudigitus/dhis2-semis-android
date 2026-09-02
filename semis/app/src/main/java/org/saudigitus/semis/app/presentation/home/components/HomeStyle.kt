package org.saudigitus.semis.app.presentation.home.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.saudigitus.semis.core.designsystem.theme.SemisAccent
import org.saudigitus.semis.core.designsystem.theme.SemisPalette
import org.saudigitus.semis.core.designsystem.theme.semisSoftShadow
import org.saudigitus.semis.core.designsystem.theme.surfaceTone
import org.saudigitus.semis.core.utils.Constants

/** Tinted page background the white cards sit on, shared with the listing screens. */
internal val HomeBackground = SemisPalette.ScreenBackground

/** Cards keep a plain white surface on top of [HomeBackground]. */
internal val HomeSurface = SemisPalette.CardSurface

/**
 * Soft elevation used across the home surfaces, mirroring the transfer module look.
 */
internal fun Modifier.softShadow(shape: Shape, elevation: Dp = 4.dp): Modifier =
    semisSoftShadow(shape, elevation)

internal object HomeAccent {
    val Blue = SemisAccent.Blue
    val Green = SemisAccent.Green
    val Purple = SemisAccent.Purple
    val Amber = SemisAccent.Amber
    val Teal = SemisAccent.Teal
}

/** Container tint used behind icons, matching the accent at low opacity. */
internal fun Color.softContainer() = surfaceTone(0.12f)

internal fun moduleAccent(module: String): Color = when (module) {
    Constants.ENROLLMENT -> HomeAccent.Blue
    Constants.ATTENDANCE, Constants.ABSENTEEISM -> HomeAccent.Green
    Constants.PERFORMANCE -> HomeAccent.Purple
    Constants.TRANSFER -> HomeAccent.Amber
    Constants.TERMS -> HomeAccent.Teal
    else -> HomeAccent.Blue
}

@Composable
internal fun HomeSectionLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 2.dp),
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
    )
}
