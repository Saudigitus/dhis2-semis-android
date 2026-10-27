package org.saudigitus.campaign.core.designsystem.templates

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.saudigitus.campaign.core.designsystem.components.model.ToolbarHeaders

@Composable
fun SimpleScaffold(
    modifier: Modifier = Modifier,
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    topBar: @Composable () -> Unit = {},
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
    content: @Composable ColumnScope.() -> Unit,
) {
    Scaffold(modifier = modifier, topBar = topBar, bottomBar = bottomBar, snackbarHost = snackbarHost,
        floatingActionButton = floatingActionButton, floatingActionButtonPosition = floatingActionButtonPosition,
        contentWindowInsets = contentWindowInsets) { padding ->
        Column(Modifier.fillMaxSize().padding(padding), content = content)
    }
}

@Composable
fun TopAppBarScaffold(
    modifier: Modifier = Modifier,
    bottomBar: @Composable () -> Unit = {},
    toolbarHeaders: ToolbarHeaders,
    navigationAction: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) = SimpleScaffold(modifier = modifier, bottomBar = bottomBar, content = content)
