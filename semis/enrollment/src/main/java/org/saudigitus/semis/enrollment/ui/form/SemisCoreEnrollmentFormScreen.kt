package org.saudigitus.semis.enrollment.ui.form

import androidx.compose.runtime.Composable
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import org.koin.core.context.loadKoinModules
import org.saudigitus.campaign.core.data.di.campaignDataModule
import org.saudigitus.campaign.core.form.di.campaignFormModule
import org.saudigitus.campaign.core.form.ui.section.FormSectionScreen
import org.saudigitus.campaign.core.navigation.AppRoute
import org.saudigitus.campaign.core.navigation.FormType

private var semisCoreFormInitialized = false

fun initializeSemisCoreForm() {
    if (semisCoreFormInitialized) return

    loadKoinModules(
        listOf(
            campaignDataModule,
            campaignFormModule,
        ),
    )
    semisCoreFormInitialized = true
}

@Composable
fun SemisCoreEnrollmentFormScreen(
    activity: FragmentActivity,
    program: String,
    orgUnit: String,
    orgUnitName: String,
    navController: NavController,
    onSaved: () -> Unit,
) {
    FormSectionScreen(
        activity = activity,
        navController = navController,
        formNav = AppRoute.FormRoute(
            formType = FormType.NEW_ENROLLMENT,
            programUid = program,
            orgUnitUid = orgUnit,
            orgUnitName = orgUnitName,
        ),
        onNewEnrollmentSaved = onSaved,
    )
}
