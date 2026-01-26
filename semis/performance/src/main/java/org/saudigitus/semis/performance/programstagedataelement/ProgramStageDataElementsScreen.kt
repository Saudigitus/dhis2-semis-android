package org.saudigitus.semis.performance.programstagedataelement

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.dhis2.ui.theme.colorPrimary
import org.hisp.dhis.mobile.ui.designsystem.theme.Radius
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.hisp.dhis.mobile.ui.designsystem.theme.dropShadow
import org.saudigitus.semis.core.designsystem.components.ConfigNotFound
import org.saudigitus.semis.core.designsystem.components.FilterDetails
import org.saudigitus.semis.core.designsystem.components.RoundedCard
import org.saudigitus.semis.core.designsystem.components.ToolbarActionState
import org.saudigitus.semis.core.designsystem.templates.TopAppBarScaffold
import org.saudigitus.semis.performance.R
import org.saudigitus.semis.performance.route.Destinations.EVENT_CAPTURE

@Composable
fun ProgramStageDataElementsScreen(
    state: ProgramStageDataElementsUiState,
    navTo: (route: String) -> Unit = {},
    navBack: () -> Unit = {},
) {
    TopAppBarScaffold(
        toolbarHeaders = state.toolbarHeaders,
        navigationAction = navBack,
        toolbarActionState = ToolbarActionState(filterVisibility = false)
    ) {
        FilterDetails(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
                .dropShadow(RoundedCornerShape(Radius.S))
                .background(
                    color = SurfaceColor.SurfaceBright,
                    shape = RoundedCornerShape(Radius.S)
                ),
            state = state.filterState.filterDetailsState,
        )

        if (state.programStageDataElements.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
            ) {
                items(state.programStageDataElements) { item ->
                    RoundedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp)
                            .dropShadow(RoundedCornerShape(Radius.S))
                            .background(
                                color = SurfaceColor.SurfaceBright,
                                shape = RoundedCornerShape(Radius.S)
                            ),
                        onClick = {
                            navTo("${EVENT_CAPTURE}/${item.programStageUid}/${item.dataElement?.uid()}")
                        }
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Book,
                                tint = colorPrimary,
                                contentDescription = null
                            )
                            Text(
                                text = item.dataElement?.displayName().orEmpty(),
                            )
                        }
                    }
                }
            }
        } else {
            ConfigNotFound(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp), message = stringResource(
                    R.string.performance_config_error
                )
            )
        }
    }
}