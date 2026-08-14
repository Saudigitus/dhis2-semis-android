package org.saudigitus.campaign.core.form.utils

import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import org.hisp.dhis.mobile.ui.designsystem.component.InputShellState
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor

object Utils {
    @Composable
    fun inputColors() = TextFieldDefaults.colors(
        focusedContainerColor = SurfaceColor.Surface,
        focusedIndicatorColor = InputShellState.FOCUSED.color,
        focusedLabelColor = InputShellState.FOCUSED.color,
        focusedLeadingIconColor = InputShellState.FOCUSED.color,
        focusedPlaceholderColor = InputShellState.FOCUSED.color,
        focusedTrailingIconColor = InputShellState.FOCUSED.color,
        unfocusedContainerColor = SurfaceColor.Surface,
        unfocusedIndicatorColor = SurfaceColor.Surface,
        disabledContainerColor = SurfaceColor.DisabledSurface,
        disabledIndicatorColor = SurfaceColor.Surface,
        disabledTextColor = InputShellState.DISABLED.color,
        disabledLabelColor = InputShellState.DISABLED.color,
        disabledPlaceholderColor = InputShellState.DISABLED.color,
        errorContainerColor = SurfaceColor.ErrorContainer,
        errorIndicatorColor = SurfaceColor.Error,
        errorCursorColor = InputShellState.ERROR.color,
        errorLabelColor = InputShellState.ERROR.color,
        errorLeadingIconColor = InputShellState.ERROR.color,
        errorTrailingIconColor = InputShellState.ERROR.color,
    )
}