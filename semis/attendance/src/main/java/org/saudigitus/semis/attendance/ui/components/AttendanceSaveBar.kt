package org.saudigitus.semis.attendance.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.saudigitus.semis.attendance.R
import org.saudigitus.semis.core.designsystem.components.buttons.TonalIconButton
import org.saudigitus.semis.core.designsystem.components.buttons.PrimaryActionButton
import org.saudigitus.semis.core.designsystem.theme.SemisPalette

/**
 * Bottom action bar of the attendance screen, holding the primary step action and the
 * form reset shortcut offered while an attendance is being taken.
 */
@Composable
internal fun AttendanceSaveBar(
    label: String,
    imageVector: ImageVector,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onReset: (() -> Unit)? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(color = SemisPalette.CardSurface)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        onReset?.let { reset ->
            TonalIconButton(
                imageVector = Icons.Default.Refresh,
                contentDescription = stringResource(R.string.reset_attendance),
                size = 48.dp,
                iconSize = 20.dp,
                containerColor = SemisPalette.ScreenBackground,
                contentColor = SemisPalette.ActionBlue,
                onClick = reset,
            )
        }

        PrimaryActionButton(
            text = label,
            imageVector = imageVector,
            enabled = enabled,
            modifier = Modifier.weight(1f),
            onClick = onClick,
        )
    }
}
