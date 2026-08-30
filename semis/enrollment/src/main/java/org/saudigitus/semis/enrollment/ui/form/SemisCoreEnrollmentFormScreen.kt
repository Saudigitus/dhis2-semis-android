package org.saudigitus.semis.enrollment.ui.form

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import org.koin.core.context.loadKoinModules
import org.saudigitus.campaign.core.data.di.campaignDataModule
import org.saudigitus.campaign.core.form.di.campaignFormModule
import org.saudigitus.campaign.core.form.ui.section.FormSectionScreen
import org.saudigitus.campaign.core.form.ui.state.FormStepProgress
import org.saudigitus.campaign.core.navigation.AppRoute
import org.saudigitus.semis.core.data.model.SyncTarget
import org.saudigitus.semis.enrollment.ui.form.summary.EnrollmentSummaryScreen

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

/**
 * Walks the user through the enrollment steps and reports back once the record has been written.
 *
 * Each step renders as a form that only gathers values: the whole enrollment is committed by
 * [EnrollmentCreationViewModel] when the last step is completed, so leaving partway writes nothing.
 */
@Composable
fun SemisCoreEnrollmentFormScreen(
    activity: FragmentActivity,
    program: String,
    orgUnit: String,
    orgUnitName: String,
    academicYear: String?,
    grade: String?,
    section: String?,
    navController: NavController,
    onSaved: (List<SyncTarget>) -> Unit,
    viewModel: EnrollmentCreationViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(program, orgUnit, academicYear, grade, section) {
        viewModel.initialize(program, orgUnit, orgUnitName, academicYear, grade, section)
    }

    // The record is written by the time the flow reports itself complete, which is the moment it
    // is worth offering to send, the same as every other capture screen in the app.
    LaunchedEffect(state.completed) {
        if (state.completed) onSaved(listOf(SyncTarget.Tracker(program)))
    }

    // Back walks the steps in reverse, and only leaves the flow from the first one.
    BackHandler(enabled = state.canGoBack) {
        viewModel.onBack()
    }

    when {
        state.completed -> {
            EnrollmentSummaryScreen(
                learnerAttributes = state.learnerAttributes,
                registrationDetails = state.registrationDetails,
                teiUid = state.tei.orEmpty(),
                stepCount = state.plan.stepCount,
                onAddAnother = viewModel::registerAnother,
                onBackToList = { onSaved(emptyList()) },
            )
        }

        state.errorMessage != null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = state.errorMessage.orEmpty())
            }
        }

        state.isProcessing -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        state.initialized -> {
            FormSectionScreen(
                activity = activity,
                navController = navController,
                formNav = AppRoute.FormRoute(
                    formType = state.currentFormType,
                    programUid = program,
                    orgUnitUid = state.orgUnit,
                    orgUnitName = state.orgUnitName.orEmpty(),
                    programStageUid = state.currentProgramStage,
                ),
                onStepCompleted = viewModel::onStepCompleted,
                onError = viewModel::onStepError,
                restoredSections = state.currentStepSections,
                prefill = state.prefill,
                onDateChanged = viewModel::onDateSelected,
                onOrgUnitChanged = viewModel::onOrgUnitSelected,
                stepProgress = FormStepProgress(
                    stepNumber = state.stepNumber,
                    stepCount = state.plan.stepCount,
                ),
                onNavigateBack = {
                    if (state.canGoBack) viewModel.onBack() else navController.navigateUp()
                },
            )
        }

        else -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}
