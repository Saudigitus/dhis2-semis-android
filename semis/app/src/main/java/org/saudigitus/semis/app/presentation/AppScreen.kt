package org.saudigitus.semis.app.presentation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.collectLatest
import org.saudigitus.semis.app.presentation.home.HomeScreen
import org.saudigitus.semis.app.presentation.home.HomeViewModel
import org.saudigitus.semis.app.presentation.navigation.HomeBarNavigation
import org.saudigitus.semis.app.presentation.navigation.NavBar
import org.saudigitus.semis.core.designsystem.components.SnackBar
import org.saudigitus.semis.core.designsystem.templates.TopAppBarScaffold
import org.saudigitus.semis.core.designsystem.theme.light_success
import org.saudigitus.semis.core.designsystem.R as DesignSystemR

@Composable
fun AppScreen(
    viewModel: HomeViewModel,
    navBack: () -> Unit,
    syncNow: () -> Unit,
    navTo: (String) -> Unit
) {
    val internalNavController = rememberNavController()
    var route by rememberSaveable { mutableStateOf(HomeBarNavigation.HOME) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // A download that succeeds closes the filters and nothing else shows it happened, so how
    // many records came down is said here.
    LaunchedEffect(Unit) {
        viewModel.downloadFeedback.collectLatest { message ->
            snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Short)
        }
    }

    TopAppBarScaffold(
        toolbarHeaders = uiState.toolbarHeaders,
        snackbarHost = {
            SnackBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                hostState = snackbarHostState,
                containerColor = light_success,
                painter = painterResource(DesignSystemR.drawable.success_icon),
            )
        },
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
        syncAction = syncNow,
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