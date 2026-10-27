package org.saudigitus.semis.core.designsystem.components.tabs

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * One pill in a [SegmentedTabRow].
 *
 * [badge] is rendered next to the label, in brackets, and is meant for counts. [icon] is
 * optional so that tabs carrying only a word stay as they were.
 */
@Immutable
data class SegmentedTabItem(
    val id: String,
    val label: String,
    val badge: String? = null,
    val icon: ImageVector? = null,
)
