package org.saudigitus.campaign.core.designsystem.utils

import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import org.hisp.dhis.mobile.ui.designsystem.component.InputShellState
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.saudigitus.campaign.core.designsystem.theme.light_error
import org.saudigitus.campaign.core.designsystem.theme.light_success
import org.saudigitus.campaign.core.designsystem.theme.light_warning

object Utils {
    @Composable
    fun inputColors() = TextFieldDefaults.colors(
        focusedContainerColor = SurfaceColor.Surface,
        focusedIndicatorColor = Color.Transparent,
        focusedLabelColor = InputShellState.FOCUSED.color,
        focusedLeadingIconColor = InputShellState.FOCUSED.color,
        focusedPlaceholderColor = InputShellState.FOCUSED.color,
        focusedTrailingIconColor = InputShellState.FOCUSED.color,
        unfocusedContainerColor = SurfaceColor.Surface,
        unfocusedIndicatorColor = Color.Transparent,
        disabledContainerColor = SurfaceColor.DisabledSurface,
        disabledIndicatorColor = Color.Transparent,
        disabledTextColor = InputShellState.DISABLED.color,
        disabledLabelColor = InputShellState.DISABLED.color,
        disabledPlaceholderColor = InputShellState.DISABLED.color,
        errorContainerColor = SurfaceColor.ErrorContainer,
        errorIndicatorColor = Color.Transparent,
        errorCursorColor = InputShellState.ERROR.color,
        errorLabelColor = InputShellState.ERROR.color,
        errorLeadingIconColor = InputShellState.ERROR.color,
        errorTrailingIconColor = InputShellState.ERROR.color,
    )

    fun getColorByProgress(progress: Float) = when {
        progress >= 0.8f -> light_success
        progress >= 0.5f -> light_warning
        else -> light_error
    }


    const val RED_THRESHOLD = 0.5f
    const val YELLOW_THRESHOLD = 0.8f
}