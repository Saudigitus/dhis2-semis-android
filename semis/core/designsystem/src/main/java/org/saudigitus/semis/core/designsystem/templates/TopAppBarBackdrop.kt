package org.saudigitus.semis.core.designsystem.templates

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import org.dhis2.ui.theme.colorPrimary
import org.saudigitus.semis.core.designsystem.components.Toolbar
import org.saudigitus.semis.core.designsystem.components.ToolbarActionState
import org.saudigitus.semis.core.designsystem.components.model.ToolbarHeaders

@Composable
fun TopAppBarBackdrop(
    modifier: Modifier = Modifier,
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    backLayerContainerColor: Color = colorPrimary,
    frontLayerContainerColor: Color = Color.White,
    toolbarHeaders: ToolbarHeaders,
    navigationAction: () -> Unit = {},
    filterAction: () -> Unit = {},
    syncAction: () -> Unit = {},
    frontLayerShape: Shape = MaterialTheme.shapes.medium
        .copy(
            topStart = CornerSize(16.dp),
            topEnd = CornerSize(16.dp),
            bottomStart = CornerSize(0.dp),
            bottomEnd = CornerSize(0.dp),
        ),
    backLayer: @Composable ColumnScope.() -> Unit,
    frontLayer: @Composable ColumnScope.() -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            Toolbar(
                headers = toolbarHeaders,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorPrimary,
                    navigationIconContentColor = Color.White,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White,
                ),
                navigationAction = navigationAction,
                disableNavigation = false,
                actionState = ToolbarActionState(
                    syncVisibility = true,
                    showFavorite = true,
                ),
                filterAction = filterAction,
                syncAction = syncAction,
            )
        },
        bottomBar = bottomBar,
        snackbarHost = snackbarHost,
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition
    ) { innerPadding ->
        Backdrop(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            backLayerContainerColor = backLayerContainerColor,
            frontLayerContainerColor = frontLayerContainerColor,
            frontLayerShape = frontLayerShape,
            backLayer = backLayer,
            frontLayer = frontLayer
        )
    }
}