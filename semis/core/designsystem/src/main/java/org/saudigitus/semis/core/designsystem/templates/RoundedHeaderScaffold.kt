package org.saudigitus.semis.core.designsystem.templates

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.saudigitus.semis.core.designsystem.theme.SemisPalette
import org.saudigitus.semis.core.designsystem.theme.SemisPalette.SurfaceBackground

/**
 * Scaffold for screens whose colored header carries its own rounded bottom corners,
 * laying the content over a flat screen background.
 */
@Composable
fun RoundedHeaderScaffold(
    header: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    containerColor: Color = SemisPalette.ScreenBackground,
    verticalSpacing: Dp = 0.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = header,
        bottomBar = bottomBar,
        snackbarHost = snackbarHost,
        containerColor = containerColor,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(color = SurfaceBackground),
            verticalArrangement = Arrangement.spacedBy(verticalSpacing, Alignment.Top),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content,
        )
    }
}
