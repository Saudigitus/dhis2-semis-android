package org.saudigitus.semis.core.form.ui

import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.saudigitus.semis.core.designsystem.theme.SemisFieldShape
import org.saudigitus.semis.core.designsystem.theme.SemisPalette

object FormFieldDefaults {

    val Shape: Shape = SemisFieldShape

    @Composable
    fun colors(): TextFieldColors = TextFieldDefaults.colors(
        focusedContainerColor = SemisPalette.ScreenBackground,
        unfocusedContainerColor = SemisPalette.ScreenBackground,
        disabledContainerColor = SurfaceColor.DisabledSurface,
        errorContainerColor = SurfaceColor.ErrorContainer,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
        errorIndicatorColor = Color.Transparent,
    )
}
