package org.saudigitus.semis.attendance.ui

import androidx.activity.compose.BackHandler
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import org.saudigitus.semis.core.data.model.SyncTarget
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import kotlinx.coroutines.flow.collectLatest
import org.saudigitus.semis.attendance.R
import org.saudigitus.semis.core.designsystem.components.bottomsheet.launchBottomSheet
import org.saudigitus.semis.core.form.ui.FormViewModel

@Composable
fun AttendanceUi(
    activity: FragmentActivity,
    viewModel: AttendanceViewModel,
    formViewModel: FormViewModel,
    navController: NavHostController,
    syncData: (List<SyncTarget>) -> Unit,
    syncNow: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarIsError by remember { mutableStateOf(false) }

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val formState by formViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.snackbarEvent.collectLatest { message ->
            if (message != null) {
                snackbarIsError = false
                formViewModel.resetCacheStatus()
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.syncEvent.collectLatest { targets ->
            syncData(targets)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.errorEvent.collectLatest { message ->
            snackbarIsError = true
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Long,
            )
        }
    }

    LaunchedEffect(state.formBuilderState) {
        formViewModel.initialize(state.formBuilderState)
    }

    if (formState.hasCachedData || formState.attendanceButtonState.hasEvent()) {
        viewModel.hasCachedValues(true)
    }

    fun navigationBack() {
        if (formState.hasCachedData) {
            launchBottomSheet(
                activity.getString(R.string.not_saved),
                activity.getString(R.string.attendance_not_saved),
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

    AttendanceScreen(
        state = state,
        formState = formState,
        snackbarHostState = snackbarHostState,
        snackbarIsError = snackbarIsError,
        onFormEvent = formViewModel::handleUiEvent,
        onEvent = {
            when (it) {
                is AttendanceUiEvent.NavBack -> {
                    navigationBack()
                }

                is AttendanceUiEvent.OnSyncClicked -> syncNow()
                is AttendanceUiEvent.OnAttendanceClick -> {
                    formViewModel.markAttendanceChanged()
                    viewModel.handleUiEvent(it)
                }
                AttendanceUiEvent.ResetForm -> {
                    formViewModel.resetCacheStatus()
                    viewModel.handleUiEvent(it)
                }
                else -> viewModel.handleUiEvent(it)
            }
        }
    )
}
