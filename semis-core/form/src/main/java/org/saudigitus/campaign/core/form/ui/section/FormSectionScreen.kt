package org.saudigitus.campaign.core.form.ui.section

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import org.koin.compose.viewmodel.koinViewModel
import org.saudigitus.campaign.core.designsystem.components.bottomsheets.launchDhis2BottomSheet
import org.saudigitus.campaign.core.designsystem.components.bottomsheets.launchDiscardBottomSheet
import org.saudigitus.campaign.core.form.R
import org.saudigitus.campaign.core.form.data.models.FormResult
import org.saudigitus.campaign.core.form.data.models.FormSectionModel
import org.saudigitus.campaign.core.form.ui.FormViewModel
import org.saudigitus.campaign.core.form.ui.screens.FormLoadErrorScreen
import org.saudigitus.campaign.core.form.ui.screens.FormShimmerScreen
import org.saudigitus.campaign.core.form.ui.state.FormEvent
import org.saudigitus.campaign.core.form.ui.state.FormSectionType
import org.saudigitus.campaign.core.form.ui.state.FormSectionUiState
import org.saudigitus.campaign.core.form.utils.completionPercentage
import org.saudigitus.campaign.core.form.utils.toFormSection
import org.saudigitus.campaign.core.navigation.AppRoute

@Composable
fun FormSectionScreen(
    modifier: Modifier = Modifier,
    activity: FragmentActivity,
    viewModel: FormViewModel = koinViewModel(),
    navController: NavController,
    formNav: AppRoute.FormRoute? = null,
    onNewEnrollmentSaved: (() -> Unit)? = null,
    onFormSaved: ((FormSectionType, FormResult) -> Unit)? = null,
    onNavigateBack: (() -> Unit)? = null,
    onStepCompleted: ((List<FormSectionModel>) -> Unit)? = null,
    onError: ((String) -> Unit)? = null,
    restoredSections: List<FormSectionModel>? = null,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Supplying a step handler is what turns this form into one leg of a longer flow: it gathers
    // the values and hands them over, and whoever drives the flow decides when anything is written.
    val collectOnly = onStepCompleted != null

    LaunchedEffect(formNav, collectOnly) {
        viewModel.initialize(
            formSection = formNav?.toFormSection(),
            ouName = formNav?.orgUnitName,
            collectOnly = collectOnly,
            restoredSections = restoredSections,
        )
    }

    LaunchedEffect(onStepCompleted) {
        if (onStepCompleted == null) return@LaunchedEffect

        viewModel.stepCompleted.collect { formSections ->
            onStepCompleted(formSections)
        }
    }

    LaunchedEffect(onError) {
        if (onError == null) return@LaunchedEffect

        viewModel.errorEvent.collect { message ->
            onError(message)
        }
    }

    LaunchedEffect(onFormSaved, onNewEnrollmentSaved) {
        viewModel.navigationEvent.collect { event ->
            val formSection = state as? FormSectionUiState.HasFormSection

            if (onFormSaved != null) {
                onFormSaved(event.formType, event.result)
                return@collect
            }

            if (formSection?.formType == FormSectionType.NEW_ENROLLMENT &&
                onNewEnrollmentSaved != null
            ) {
                onNewEnrollmentSaved()
                return@collect
            }

            if (event.route != null) {
                navController.navigate(event.route)
            } else {
                when(formSection?.formType) {
                    FormSectionType.NEW_ENROLLMENT -> {
                        navController.popBackStack<AppRoute.TrackerListingRoute>(false)
                    }
                    FormSectionType.NEW_EVENT_WITH_REGISTRATION -> {
                        if (formSection.previousType == FormSectionType.NEW_ENROLLMENT) {
                            navController.popBackStack<AppRoute.TrackerListingRoute>(false)
                        } else {
                            navController.navigateUp()
                        }
                    }
                    else -> {
                        navController.popBackStack<AppRoute.EventRoute>(false)
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.handleSave.collect { status ->
            if (status) {
                launchDhis2BottomSheet(
                    title = activity.getString(R.string.save),
                    subtitle = activity.getString(R.string.save_changes_form),
                    supportFragmentManager = activity.supportFragmentManager,
                    onCancel = { viewModel.handleUiEvent(FormEvent.CancelSave) },
                    onSave = { viewModel.handleUiEvent(FormEvent.SaveEvent) }
                )
            }
        }
    }

    fun backAction() {
        val newState = state as? FormSectionUiState.HasFormSection
        val hasData = (newState?.formSections?.completionPercentage() ?: 0f) > 0f

        if (hasData) {
            launchDiscardBottomSheet(
                activity.getString(R.string.not_saved),
                activity.getString(R.string.unsaved_changes_form),
                supportFragmentManager = activity.supportFragmentManager,
                onDiscard = {
                    viewModel.reset()
                    if (onNavigateBack != null) {
                        onNavigateBack()
                    } else if (newState?.formType == FormSectionType.NEW_EVENT_WITH_REGISTRATION) {
                        navController.popBackStack<AppRoute.TrackerDetailRoute>(false)
                    } else {
                        navController.navigateUp()
                    }
                },
                onKeepEdition = { },
            )
        } else {
            viewModel.reset()
            if (onNavigateBack != null) {
                onNavigateBack()
            } else if (newState?.formType == FormSectionType.NEW_EVENT_WITH_REGISTRATION) {
                navController.popBackStack<AppRoute.TrackerDetailRoute>(false)
            } else {
                navController.navigateUp()
            }
        }
    }

    BackHandler {
        backAction()
    }

    when (state) {
        is FormSectionUiState.Idle -> {
            FormLoadErrorScreen { backAction() }
        }

        is FormSectionUiState.Loading -> {
            FormShimmerScreen()
        }

        is FormSectionUiState.HasFormSection -> {
            val formSection = state as FormSectionUiState.HasFormSection

            FormSectionUi(
                modifier = modifier,
                state = formSection,
            ) { event ->
                when (event) {
                    is FormEvent.NavigateBack -> {
                        backAction()
                    }

                    else -> viewModel.handleUiEvent(event)
                }
            }
        }
    }
}
