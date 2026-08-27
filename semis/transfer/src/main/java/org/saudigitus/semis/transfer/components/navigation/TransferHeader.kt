package org.saudigitus.semis.transfer.components.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import org.saudigitus.semis.core.designsystem.components.Toolbar
import org.saudigitus.semis.core.designsystem.components.model.ToolbarHeaders
import org.saudigitus.semis.core.designsystem.theme.SemisPalette
import org.saudigitus.semis.transfer.R
import org.saudigitus.semis.transfer.model.TransferStep
import org.saudigitus.semis.transfer.model.TransferTab

/**
 * Transfer screen header.
 *
 * The same bar the home, the listings and the other SEMIS screens are given by their
 * shared scaffold, with the same colour, the same title sizes and the same placing of the
 * actions. The second row names the school and the academic year the lists are read for,
 * and the sheet below carries the two lists.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TransferHeader(
    schoolContext: String,
    selectedTab: TransferTab,
    requestStep: TransferStep?,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    onRefreshIncoming: () -> Unit,
) {
    Toolbar(
        headers = ToolbarHeaders(
            title = stringResource(R.string.transfer),
            subtitle = requestStep?.let { stringResource(it.subtitleResource()) }
                ?: schoolContext.takeIf { it.isNotBlank() },
        ),
        modifier = modifier,
        navigationAction = onNavigateBack,
        // This overload of the toolbar draws the back arrow when the flag is true, the
        // opposite of what its name suggests and of what the other overload does. The
        // title is offset to sit next to that arrow, so leaving it out also pulls the
        // title off the left edge.
        disableNavigation = true,
        colors = TransferToolbarColors,
        actions = {
            // Requests addressed to this school live in the school that raised them, so
            // they are fetched rather than found, and only the incoming list can be.
            if (requestStep == null && selectedTab == TransferTab.INCOMING) {
                IconButton(onClick = onRefreshIncoming) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.refresh_incoming),
                    )
                }
            }
        },
    )
}

/** The colours every other SEMIS screen gives its bar, taken from the shared scaffold. */
@OptIn(ExperimentalMaterial3Api::class)
private val TransferToolbarColors: TopAppBarColors
    @Composable get() = TopAppBarDefaults.topAppBarColors(
        containerColor = SemisPalette.HeaderBlueAccent,
        navigationIconContentColor = Color.White,
        titleContentColor = Color.White,
        actionIconContentColor = Color.White,
    )

private fun TransferStep.subtitleResource(): Int = when (this) {
    TransferStep.ENTITIES -> R.string.step_select
    TransferStep.DESTINATION -> R.string.step_destination
    TransferStep.REVIEW -> R.string.step_review
}
