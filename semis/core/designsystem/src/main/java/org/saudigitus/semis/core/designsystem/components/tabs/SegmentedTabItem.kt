package org.saudigitus.semis.core.designsystem.components.tabs

import androidx.compose.runtime.Immutable

/**
 * One choice of a [SegmentedTabRow].
 */
@Immutable
data class SegmentedTabItem(
    val id: String,
    val label: String,
    val badge: String? = null,
)
