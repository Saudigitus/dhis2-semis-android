package org.saudigitus.semis.performance.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.saudigitus.semis.core.data.model.SearchTeiModel
import org.saudigitus.semis.core.designsystem.filters.FilterComponentState
import org.saudigitus.semis.core.designsystem.utils.mapper.TEICardMapper
import org.saudigitus.semis.core.form.ui.FormViewModel
import org.saudigitus.semis.performance.performanceevent.PerformanceEventCaptureScreen
import org.saudigitus.semis.performance.performanceevent.PerformanceViewModel
import org.saudigitus.semis.performance.route.Destinations.EVENT_CAPTURE
import org.saudigitus.semis.performance.route.Destinations.PROGRAM_STAGE
import org.saudigitus.semis.performance.route.Destinations.PROGRAM_STAGE_DATA_ELEMENTS

@Composable
fun PerformanceNavGraph(
    activity: FragmentActivity,
    program: String,
    orgUnit: String,
    teiList: List<SearchTeiModel> = emptyList(),
    teiCardMapper: TEICardMapper,
    filterState: FilterComponentState? = null,
    parentNavController: NavHostController? = null,
    syncData: (() -> Unit)? = null,
) {
    val navController = rememberNavController()

    NavHost(navController, startDestination = EVENT_CAPTURE) {
        composable(PROGRAM_STAGE) {

        }
        composable(
            route = PROGRAM_STAGE_DATA_ELEMENTS,
            arguments = listOf(
                navArgument("programStage") { type = NavType.StringType }
            )
        ) { entry ->
            val programStage = entry.arguments?.getString("programStage").orEmpty()

        }
        composable(
            route = "${EVENT_CAPTURE}/{programStage}/{dataElement}",
            arguments = listOf(
                navArgument("programStage") { type = NavType.StringType },
                navArgument("dataElement") { type = NavType.StringType },
            )
        ) { entry ->
            val programStage = entry.arguments?.getString("programStage").orEmpty()
            val dataElement = entry.arguments?.getString("dataElement").orEmpty()

            val viewModel = hiltViewModel<PerformanceViewModel>()
            val formViewModel = hiltViewModel<FormViewModel>()

            LaunchedEffect(Unit) {
                viewModel.initialize(
                    program = program,
                    orgUnit = orgUnit,
                    programStage = programStage,
                    dataElement = dataElement,
                    tei = teiList,
                    filterState = filterState!!,
                    initFormFieldData = formViewModel::collectIndividualFormEvent
                )
            }

            PerformanceEventCaptureScreen(
                activity = activity,
                viewModel = viewModel,
                formViewModel = formViewModel,
                teiCardMapper = teiCardMapper,
                navController = navController,
                syncData = syncData!!,
            )
        }
    }
}