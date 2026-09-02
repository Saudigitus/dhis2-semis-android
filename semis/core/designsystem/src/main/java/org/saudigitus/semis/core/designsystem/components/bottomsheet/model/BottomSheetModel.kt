package org.saudigitus.semis.core.designsystem.components.bottomsheet.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector


data class BottomSheetModel(
    val icon: ImageVector? = null,
    val iconName: String? = null,
    val label: String? = null,
    val value: String? = null,
    val color: Color? = null,
)
