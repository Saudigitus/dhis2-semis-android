package org.saudigitus.campaign.core.form.navigation

import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import org.saudigitus.campaign.core.form.ui.FormViewModel
import org.saudigitus.campaign.core.form.ui.section.FormSectionScreen
import org.saudigitus.campaign.core.navigation.AppRoute


fun NavGraphBuilder.formGraph(
    navController: NavController,
    activity: FragmentActivity,
    viewModel: FormViewModel
) {
    composable<AppRoute.FormRoute> {
        val route = it.toRoute<AppRoute.FormRoute>()

        FormSectionScreen(
            navController = navController,
            viewModel = viewModel,
            activity = activity,
            formNav = route,
        )
    }
}
