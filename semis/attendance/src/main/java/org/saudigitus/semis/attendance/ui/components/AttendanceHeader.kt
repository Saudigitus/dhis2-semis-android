package org.saudigitus.semis.attendance.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.saudigitus.semis.core.designsystem.components.Toolbar
import org.saudigitus.semis.core.designsystem.components.ToolbarActionState
import org.saudigitus.semis.core.designsystem.components.model.ToolbarHeaders
import org.saudigitus.semis.core.designsystem.theme.SemisPalette

/**
 * Attendance screen header.
 *
 * The same bar the home, the listings and the performance screens are given by their own
 * scaffold, with the same colour, the same title and the same placing of the actions.
 * Attendance used to build a header of its own, which is why it looked like a screen from a
 * different app.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AttendanceHeader(
    headers: ToolbarHeaders,
    modifier: Modifier = Modifier,
    dateValidator: (Long) -> Boolean = { true },
    onNavigateBack: () -> Unit,
    onSync: () -> Unit,
    onDateSelected: (String) -> Unit,
) {
    Toolbar(
        modifier = modifier,
        headers = headers,
        colors = AttendanceToolbarColors,
        navigationAction = onNavigateBack,
        disableNavigation = false,
        actionState = ToolbarActionState(
            syncVisibility = true,
            filterVisibility = false,
            showCalendar = true,
        ),
        calendarAction = onDateSelected,
        dateValidator = dateValidator,
        syncAction = onSync,
    )
}

/** The colours every other SEMIS screen gives its bar, taken from the shared scaffold. */
@OptIn(ExperimentalMaterial3Api::class)
private val AttendanceToolbarColors: TopAppBarColors
    @Composable get() = TopAppBarDefaults.topAppBarColors(
        containerColor = SemisPalette.HeaderBlueAccent,
        navigationIconContentColor = Color.White,
        titleContentColor = Color.White,
        actionIconContentColor = Color.White,
    )
