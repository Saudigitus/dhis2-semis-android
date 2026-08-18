package org.saudigitus.semis.transfer

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.saudigitus.semis.core.designsystem.utils.softShadow as designSystemSoftShadow

fun Modifier.softShadow(shape: Shape, elevation: Dp = 10.dp): Modifier =
    designSystemSoftShadow(shape = shape, elevation = elevation)
