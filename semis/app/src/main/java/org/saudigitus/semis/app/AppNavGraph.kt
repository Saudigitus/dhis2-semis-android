package org.saudigitus.semis.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.saudigitus.semis.app.presentation.AppScreen
import org.saudigitus.semis.app.presentation.home.HomeViewModel
import org.saudigitus.semis.app.presentation.navigation.AppRoutes
import org.saudigitus.semis.app.presentation.tei.TeiListEvent
import org.saudigitus.semis.app.presentation.tei.TeiListScreen
import org.saudigitus.semis.attendance.ui.AttendanceUi
import org.saudigitus.semis.attendance.ui.AttendanceViewModel
import org.saudigitus.semis.core.designsystem.utils.mapper.TEICardMapper
import org.saudigitus.semis.core.form.ui.FormViewModel
import org.saudigitus.semis.performance.route.PerformanceNavGraph

@Composable
fun AppNavGraph(
    activity: FragmentActivity,
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
        composable(route = AppRoutes.ATTENDANCE) {
            val attendanceViewModel = hiltViewModel<AttendanceViewModel>()
            val formViewModel = hiltViewModel<FormViewModel>()

            val homeState by viewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(key1 = Unit) {
                attendanceViewModel.initialize(
                    homeState.program,
                    homeState.filterState.orgUnit?.uid.orEmpty(),
                    homeState.tei,
                    homeState.filterState.filterDetailsState
                )
            }

            AttendanceUi(
                activity = activity,
                viewModel = attendanceViewModel,
                formViewModel = formViewModel,
                teiCardMapper = teiCardMapper,
                navController = navController,
                syncData = syncData
            )
        }
        composable(route = AppRoutes.PERFORMANCE) {
            val homeState by viewModel.uiState.collectAsStateWithLifecycle()

            PerformanceNavGraph(
                activity,
                homeState.program,
                homeState.filterState.orgUnit?.uid.orEmpty(),
                homeState.tei,
                teiCardMapper,
                homeState.filterState,
                navController,
                syncData,
            )
        }
    }
}