package org.saudigitus.semis.core.designsystem.components.filters

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * A single label/value pair of the applied filter context rendered by [FilterInfoCard].
 */
@Immutable
data class FilterInfoItem(
    val label: String,
    val value: String,
    val icon: ImageVector,
)
