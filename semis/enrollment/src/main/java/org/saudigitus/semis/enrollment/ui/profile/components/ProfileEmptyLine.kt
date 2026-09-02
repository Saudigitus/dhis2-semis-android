package org.saudigitus.semis.enrollment.ui.profile.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.saudigitus.semis.core.designsystem.components.text.SectionCaption

/**
 * Stated absence of data inside a detail card, so an empty section still reads as an
 * answer rather than a rendering gap.
 */
@Composable
internal fun ProfileEmptyLine(
    text: String,
    modifier: Modifier = Modifier,
) {
    SectionCaption(text = text, modifier = modifier)
}
