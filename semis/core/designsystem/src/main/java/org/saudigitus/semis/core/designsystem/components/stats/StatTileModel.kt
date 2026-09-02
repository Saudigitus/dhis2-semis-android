package org.saudigitus.semis.core.designsystem.components.stats

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * A single value/label pair rendered by [StatTile].
 */
@Immutable
data class StatTileModel(
    val id: String,
    val label: String,
    val value: Int,
    val containerColor: Color,
    val contentColor: Color = Color.White,
)
