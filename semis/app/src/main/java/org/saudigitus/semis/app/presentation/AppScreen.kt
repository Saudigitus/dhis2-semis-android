package org.saudigitus.semis.app.presentation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.saudigitus.semis.app.presentation.home.HomeScreen
import org.saudigitus.semis.app.presentation.home.HomeViewModel
import org.saudigitus.semis.app.presentation.navigation.HomeBarNavigation
import org.saudigitus.semis.app.presentation.navigation.NavBar
import org.saudigitus.semis.core.designsystem.templates.TopAppBarScaffold

@Composable
fun AppScreen(
    viewModel: HomeViewModel,
    navBack: () -> Unit,
    syncData: () -> Unit,
    navTo: (String) -> Unit
) {
    val internalNavController = rememberNavController()
    var route by rememberSaveable { mutableStateOf(HomeBarNavigation.HOME) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TopAppBarScaffold(
        toolbarHeaders = uiState.toolbarHeaders,
        navigationAction = navBack,
        bottomBar = {
            NavBar(destination = route.ordinal) {
                route = when (it) {
                    HomeBarNavigation.HOME.ordinal -> HomeBarNavigation.HOME
                    HomeBarNavigation.ANALYTICS.ordinal -> HomeBarNavigation.ANALYTICS
                    else -> HomeBarNavigation.NONE
                }
            }
        },
        syncAction = syncData,
        filterAction = viewModel::hideShowFilter
    ) {
        NavHost(
            internalNavController,
            startDestination = route.name,
        ) {
            composable(HomeBarNavigation.HOME.name) {
                HomeScreen(
                    state = uiState,
                    onFilterEvent = viewModel::handleFilterEvent,
                    navTo = navTo
                )
            }
            composable(HomeBarNavigation.ANALYTICS.name) {
                Text(text = "Analytics")
            }
        }
    }
}