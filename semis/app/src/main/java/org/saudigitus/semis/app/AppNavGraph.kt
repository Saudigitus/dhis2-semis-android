package org.saudigitus.semis.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.saudigitus.semis.app.presentation.AppScreen
import org.saudigitus.semis.app.presentation.home.HomeViewModel
import org.saudigitus.semis.app.presentation.navigation.AppRoutes
import org.saudigitus.semis.app.presentation.tei.TeiListEvent
import org.saudigitus.semis.app.presentation.tei.TeiListScreen
import org.saudigitus.semis.core.designsystem.utils.mapper.TEICardMapper

@Composable
fun AppNavGraph(
    viewModel: HomeViewModel,
    teiCardMapper: TEICardMapper,
    navController: NavHostController,
    navBack: () -> Unit,
    syncData: () -> Unit,
    displayImageDetail: (imagePath: String) -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = AppRoutes.HOME,
    ) {
        composable(route = AppRoutes.HOME) {
            AppScreen(
                viewModel = viewModel,
                navBack = navBack,
                syncData = syncData,
                navTo = navController::navigate
            )
        }
        composable(route = AppRoutes.TRACKER_LIST) {
            val state by viewModel.uiState.collectAsStateWithLifecycle()

            TeiListScreen(
                state = state,
                teiCardMapper = teiCardMapper,
                onEvent = {
                    when (it) {
                        is TeiListEvent.OnBack -> navController.navigateUp()
                        is TeiListEvent.OnSyncClick -> syncData()
                        is TeiListEvent.OnTeiClick -> {

                        }
                        is TeiListEvent.DisplayImageDetail -> displayImageDetail(it.imagePath)
                    }
                }
            )
        }
    }
}