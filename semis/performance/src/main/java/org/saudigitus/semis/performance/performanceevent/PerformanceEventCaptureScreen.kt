package org.saudigitus.semis.performance.performanceevent

import androidx.activity.compose.BackHandler
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import kotlinx.coroutines.flow.collectLatest
import org.saudigitus.semis.core.designsystem.R
import org.saudigitus.semis.core.designsystem.components.bottomsheet.launchBottomSheet
import org.saudigitus.semis.core.designsystem.utils.mapper.TEICardMapper
import org.saudigitus.semis.core.form.ui.FormViewModel

@Composable
fun PerformanceEventCaptureScreen(
    activity: FragmentActivity,
    viewModel: PerformanceViewModel,
    formViewModel: FormViewModel,
    teiCardMapper: TEICardMapper,
    navController: NavHostController,
    syncData: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val formState by formViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.snackbarEvent.collectLatest { message ->
            if (message != null) {
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    LaunchedEffect(state.formBuilderState) {
        formViewModel.initialize(state.formBuilderState)
    }

    LaunchedEffect(formState.fieldsData) {
        viewModel.updateFormFields(formState.fieldsData)
        formViewModel.collectIndividualSummary {
            viewModel.performanceSummary(it)
        }
    }

    fun navigationBack() {
        if (formState.hasCachedData) {
            launchBottomSheet(
                activity.getString(R.string.not_saved),
                activity.getString(R.string.performance_not_saved),
                supportFragmentManager = activity.supportFragmentManager,
                onDiscard = {
                    viewModel.resetForm()
                    navController.navigateUp()
                },
                onKeepEdition = { },
            )
        } else {
            viewModel.disableEditing().also {
                navController.navigateUp()
            }
        }
    }

    BackHandler {
        navigationBack()
    }

    PerformanceEventCapture(
        state = state,
        formState = formState,
        snackbarHostState = snackbarHostState,
        teiCardMapper = teiCardMapper,
        onFormEvent = formViewModel::handleUiEvent,
        onEvent = {
            when (it) {
                is PerformanceUiEvent.NavBack -> {
                    navigationBack()
                }

                is PerformanceUiEvent.Sync -> syncData()
                else -> viewModel.handleUiEvent(
                    it,
                    formViewModel::enableForm,
                    formViewModel::resetCacheStatus
                )
            }
        }
    )
}