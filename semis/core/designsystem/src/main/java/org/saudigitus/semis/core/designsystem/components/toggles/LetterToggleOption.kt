package org.saudigitus.semis.core.designsystem.components.toggles

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * A single choice of a [LetterToggleGroup], rendered as the leading letter of its label.
 */
@Immutable
data class LetterToggleOption(
    val id: String,
    val letter: String,
    val label: String,
    val selectedContainerColor: Color,
    val containerColor: Color,
    val contentColor: Color,
    val borderColor: Color,
    val selectedContentColor: Color = Color.White,
    val enabled: Boolean = true,
)
