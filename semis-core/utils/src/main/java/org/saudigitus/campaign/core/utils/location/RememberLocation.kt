package org.saudigitus.campaign.core.utils.location

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.cancel
import kotlinx.coroutines.withTimeoutOrNull
import org.saudigitus.campaign.core.utils.R
import org.saudigitus.campaign.core.utils.location.state.CoordinateState
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun rememberCoordinateState(
    targetAccuracy: Float = 5f,
    timeoutMillis: Long = 15_000L,
    captureKey: Any? = Unit,
    enabled: Boolean = true
): CoordinateState {

    val context = LocalContext.current
    val tracker = remember { LocationTracker(context) }

    var state by remember(captureKey) {
        mutableStateOf(CoordinateState(isLoading = enabled))
    }

    var permissionRequested by remember { mutableStateOf(false) }
    var permissionGranted by remember { mutableStateOf(false) }

    if (enabled && !permissionRequested) {
        RequestLocationPermissions { granted ->
            permissionRequested = true
            permissionGranted = granted

            if (!granted) {
                state = state.copy(
                    isLoading = false,
                    error = context.getString(R.string.no_location_permission)
                )
            }
        }
    }


    LaunchedEffect(enabled, permissionRequested, permissionGranted, captureKey) {
        if (!enabled) {
            state = state.copy(isLoading = false)
            return@LaunchedEffect
        }

        if (!permissionRequested || !permissionGranted) return@LaunchedEffect

        state = state.copy(isLoading = true, error = null)

        withTimeoutOrNull(timeoutMillis.milliseconds) {
            tracker.getLocationUpdates()
                .collect { location ->

                    state = state.copy(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        accuracy = location.accuracy,
                        isLoading = true
                    )

                    if (location.accuracy <= targetAccuracy) {
                        state = state.copy(isLoading = false)
                        cancel()
                    }
                }
        } ?: run {
            state = state.copy(
                isLoading = false,
                error = "Could not reach $targetAccuracy m accuracy"
            )
        }
    }


    return state
}
