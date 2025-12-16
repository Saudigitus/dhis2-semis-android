package org.saudigitus.semis.performance.route

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.saudigitus.semis.core.data.model.SearchTeiModel
import org.saudigitus.semis.core.designsystem.filters.FilterComponentState
import org.saudigitus.semis.core.designsystem.utils.mapper.TEICardMapper
import org.saudigitus.semis.performance.route.Destinations.EVENT_CAPTURE
import org.saudigitus.semis.performance.route.Destinations.PROGRAM_STAGE
import org.saudigitus.semis.performance.route.Destinations.PROGRAM_STAGE_DATA_ELEMENTS

@Composable
fun PerformanceNavGraph(
    program: String,
    teiList: List<SearchTeiModel> = emptyList(),
    teiCardMapper: TEICardMapper,
    filterState: FilterComponentState? = null,
    parentNavController: NavHostController? = null,
    syncData: (() -> Unit)? = null,
) {
    val navController = rememberNavController()

    NavHost(navController, startDestination = PROGRAM_STAGE) {
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
            route = EVENT_CAPTURE,
            arguments = listOf(
                navArgument("programStage") { type = NavType.StringType },
                navArgument("dataElement") { type = NavType.StringType },
            )
        ) { entry ->
            val programStage = entry.arguments?.getString("programStage").orEmpty()
            val dataElement = entry.arguments?.getString("dataElement").orEmpty()


        }
    }
}