package org.saudigitus.campaign.core.designsystem.templates

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.FabPosition
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.saudigitus.campaign.core.designsystem.components.Toolbar
import org.saudigitus.campaign.core.designsystem.components.ToolbarActionState
import org.saudigitus.campaign.core.designsystem.components.model.ToolbarHeaders

@Composable
fun TopAppBarScaffold(
    modifier: Modifier = Modifier,
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    toolbarHeaders: ToolbarHeaders,
    topAppBarColors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = SurfaceColor.Primary,
        navigationIconContentColor = Color.White,
        titleContentColor = Color.White,
        actionIconContentColor = Color.White,
    ),
    toolbarActionState: ToolbarActionState = ToolbarActionState(
        syncVisibility = true,
        showCustomAction = true,
    ),
    navigationAction: () -> Unit = {},
    calendarAction: (String) -> Unit = {},
    filterAction: () -> Unit = {},
    syncAction: () -> Unit = {},
    customAction: @Composable (() -> Unit) = {},
    dateValidator: (Long) -> Boolean = { true },
    content: @Composable ColumnScope.() -> Unit,
) {
    SimpleScaffold(
        modifier = modifier,
        bottomBar = bottomBar,
        snackbarHost = snackbarHost,
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        topBar = {
            Toolbar(
                headers = toolbarHeaders,
                colors = topAppBarColors,
                navigationAction = navigationAction,
                disableNavigation = false,
                actionState = toolbarActionState,
                calendarAction = calendarAction,
                filterAction = filterAction,
                syncAction = syncAction,
                customAction = customAction,
                dateValidator = dateValidator,
            )
        },
        content = content,
    )
}