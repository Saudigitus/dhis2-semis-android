package org.saudigitus.semis.attendance.ui

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavHostController
import kotlinx.coroutines.flow.collectLatest
import org.saudigitus.semis.attendance.R
import org.saudigitus.semis.core.designsystem.components.bottomsheet.launchBottomSheet
import org.saudigitus.semis.core.designsystem.utils.mapper.TEICardMapper

@Composable
fun AttendanceUi(
    activity: FragmentActivity,
    viewModel: AttendanceViewModel,
    state: AttendanceUiState,
    teiCardMapper: TEICardMapper,
    navController: NavHostController,
    syncData: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }


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

    AttendanceScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        teiCardMapper = teiCardMapper,
        onEvent = {
            when (it) {
                is AttendanceUiEvent.NavBack -> {
                    if (state.hasDataToSave) {
                        launchBottomSheet(
                            activity.getString(R.string.not_saved),
                            activity.getString(R.string.attendance_not_saved),
                            supportFragmentManager = activity.supportFragmentManager,
                            onDiscard = { navController.navigateUp() },
                            onKeepEdition = {  },
                        )
                    } else {
                        navController.navigateUp()
                    }
                }
                is AttendanceUiEvent.OnSyncClicked -> syncData()
                else -> viewModel.handleUiEvent(it)
            }
        }
    )
}