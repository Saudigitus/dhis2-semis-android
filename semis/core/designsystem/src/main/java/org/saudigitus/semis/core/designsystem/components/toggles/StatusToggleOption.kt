package org.saudigitus.semis.core.designsystem.components.toggles

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * A single choice of a [StatusToggleGroup], rendered as the icon of the status it stands
 * for and colored after it.
 */
@Immutable
data class StatusToggleOption(
    val id: String,
    val icon: ImageVector,
    val label: String,
    val selectedContainerColor: Color,
    val containerColor: Color,
    val contentColor: Color,
    val borderColor: Color,
    val selectedContentColor: Color = Color.White,
    val enabled: Boolean = true,
)
