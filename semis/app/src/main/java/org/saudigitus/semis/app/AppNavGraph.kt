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
import org.saudigitus.semis.enrollment.ui.EnrollmentScreen
import org.saudigitus.semis.enrollment.ui.profile.StudentProfileEvent
import org.saudigitus.semis.enrollment.ui.profile.StudentProfileScreen
import org.saudigitus.semis.enrollment.ui.profile.StudentProfileViewModel
import org.saudigitus.semis.enrollment.ui.form.SemisCoreEnrollmentFormScreen
import org.saudigitus.semis.enrollment.ui.form.initializeSemisCoreForm
import org.saudigitus.semis.core.designsystem.utils.mapper.TEICardMapper
import org.saudigitus.semis.core.form.ui.FormViewModel
import org.saudigitus.semis.performance.route.PerformanceNavGraph
import org.saudigitus.semis.transfer.TransferUi
import org.saudigitus.semis.transfer.TransferViewModel

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
            // Coming back from a module must show what the module changed: an enrollment
            // adds a learner to the class, and a transfer decision can take one away.
            LaunchedEffect(Unit) { viewModel.refreshTeis() }
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
                            navController.navigate(AppRoutes.studentProfile(it.tei))
                        }

                        is TeiListEvent.DisplayImageDetail -> displayImageDetail(it.imagePath)
                    }
                }
            )
        }
        composable(route = AppRoutes.STUDENT_PROFILE_ROUTE) { entry ->
            val homeState by viewModel.uiState.collectAsStateWithLifecycle()
            val profileViewModel = hiltViewModel<StudentProfileViewModel>()
            val teiUid = entry.arguments
                ?.getString(AppRoutes.STUDENT_PROFILE_ARG_TEI)
                .orEmpty()

            LaunchedEffect(key1 = teiUid, key2 = homeState.program) {
                if (teiUid.isNotBlank() && homeState.program.isNotBlank()) {
                    profileViewModel.initialize(
                        teiUid = teiUid,
                        program = homeState.program,
                        filterDetailsState = homeState.filterState.filterDetailsState,
                    )
                }
            }

            val profileState by profileViewModel.uiState.collectAsStateWithLifecycle()

            StudentProfileScreen(
                state = profileState,
                onEvent = { event ->
                    when (event) {
                        is StudentProfileEvent.OnBack -> navController.navigateUp()
                        else -> profileViewModel.handleEvent(event)
                    }
                },
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
                navController = navController,
                syncData = syncData
            )
        }
        composable(route = AppRoutes.ENROLLMENT) {
            val homeState by viewModel.uiState.collectAsStateWithLifecycle()

            EnrollmentScreen(
                programName = homeState.programName,
                tei = homeState.tei,
                filterState = homeState.filterState,
                onBack = navController::navigateUp,
                onSync = syncData,
                onNewEnrollment = {
                    initializeSemisCoreForm()
                    navController.navigate(AppRoutes.ENROLLMENT_FORM)
                },
                onTeiClick = { teiUid ->
                    navController.navigate(AppRoutes.studentProfile(teiUid))
                },
            )
        }
        composable(route = AppRoutes.ENROLLMENT_FORM) {
            val homeState by viewModel.uiState.collectAsStateWithLifecycle()
            val orgUnit = homeState.filterState.orgUnit

            SemisCoreEnrollmentFormScreen(
                activity = activity,
                program = homeState.program,
                orgUnit = orgUnit?.uid.orEmpty(),
                orgUnitName = orgUnit?.displayName.orEmpty(),
                navController = navController,
                onSaved = {
                    viewModel.refreshTeis()
                    navController.navigateUp()
                },
            )
        }
        composable(route = AppRoutes.PERFORMANCE) {
            val homeState by viewModel.uiState.collectAsStateWithLifecycle()

            PerformanceNavGraph(
                activity,
                homeState.program,
                homeState.filterState.orgUnit?.uid.orEmpty(),
                homeState.tei,
                homeState.filterState,
                navController,
                syncData,
            )
        }
        composable(route = AppRoutes.TRANSFER) {
            val transferViewModel = hiltViewModel<TransferViewModel>()
            val formViewModel = hiltViewModel<FormViewModel>()
            val homeState by viewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(homeState.program, homeState.filterState.orgUnit) {
                homeState.filterState.orgUnit?.let { sourceOrgUnit ->
                    transferViewModel.initialize(
                        program = homeState.program,
                        sourceOrgUnit = sourceOrgUnit,
                        records = homeState.tei,
                        originFilterDetails = homeState.filterState.filterDetailsState,
                    )
                }
            }

            TransferUi(
                viewModel = transferViewModel,
                formViewModel = formViewModel,
                navigateBack = navController::navigateUp,
                syncData = syncData,
            )
        }
    }
}
