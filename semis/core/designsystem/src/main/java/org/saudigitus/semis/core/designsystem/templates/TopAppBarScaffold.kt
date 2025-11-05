package org.saudigitus.semis.core.designsystem.templates

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.FabPosition
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.dhis2.ui.theme.colorPrimary
import org.saudigitus.semis.core.designsystem.components.Toolbar
import org.saudigitus.semis.core.designsystem.components.ToolbarActionState
import org.saudigitus.semis.core.designsystem.components.model.ToolbarHeaders

@Composable
fun TopAppBarScaffold(
    modifier: Modifier = Modifier,
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    toolbarHeaders: ToolbarHeaders,
    topAppBarColors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = colorPrimary,
        navigationIconContentColor = Color.White,
        titleContentColor = Color.White,
        actionIconContentColor = Color.White,
    ),
    toolbarActionState: ToolbarActionState = ToolbarActionState(
        syncVisibility = true,
        showFavorite = true,
    ),
    navigationAction: () -> Unit = {},
    calendarAction: (String) -> Unit = {},
    filterAction: () -> Unit = {},
    syncAction: () -> Unit = {},
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
                dateValidator = dateValidator,
            )
        },
        content = content,
    )
}