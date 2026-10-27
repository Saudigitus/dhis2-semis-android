package org.saudigitus.campaign.core.form.utils

import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import org.hisp.dhis.mobile.ui.designsystem.component.InputShellState
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.saudigitus.campaign.core.designsystem.theme.FormSurfaces

object Utils {
    /**
     * Fields are drawn as tinted boxes over the white section cards, so the indicator line of the
     * filled text field is dropped and the error state is carried by the container and the
     * supporting text.
     */
    @Composable
    fun inputColors() = TextFieldDefaults.colors(
        focusedContainerColor = FormSurfaces.FieldSurface,
        focusedIndicatorColor = Color.Transparent,
        focusedLabelColor = InputShellState.FOCUSED.color,
        focusedLeadingIconColor = InputShellState.FOCUSED.color,
        focusedTrailingIconColor = InputShellState.FOCUSED.color,
        unfocusedContainerColor = FormSurfaces.FieldSurface,
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
}
