package org.saudigitus.semis.performance.programstage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.hisp.dhis.mobile.ui.designsystem.theme.Radius
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.hisp.dhis.mobile.ui.designsystem.theme.dropShadow
import org.saudigitus.semis.core.designsystem.components.ConfigNotFound
import org.saudigitus.semis.core.designsystem.components.FilterDetails
import org.saudigitus.semis.core.designsystem.components.ToolbarActionState
import org.saudigitus.semis.core.designsystem.components.cards.ActionCard
import org.saudigitus.semis.core.designsystem.templates.TopAppBarScaffold
import org.saudigitus.semis.core.designsystem.utils.ModuleIcons
import org.saudigitus.semis.core.utils.Constants.PERFORMANCE
import org.saudigitus.semis.core.utils.Constants.TERMS
import org.saudigitus.semis.performance.R


@Composable
fun ProgramStageScreen(
    state: ProgramStageUiState,
    navTo: (route: String) -> Unit = {},
    navBack: () -> Unit = {},
){
    TopAppBarScaffold(
        toolbarHeaders = state.toolbarHeaders,
        navigationAction = navBack,
        toolbarActionState = ToolbarActionState(filterVisibility = false)
    ) {
        FilterDetails(
            modifier = Modifier.fillMaxWidth()
                .padding(5.dp)
                .dropShadow(RoundedCornerShape(Radius.S))
                .background(
                    color = SurfaceColor.SurfaceBright,
                    shape = RoundedCornerShape(Radius.S)
                ),
            state = state.filterState.filterDetailsState,

        )

        if (state.programStages.isNotEmpty()) {
            LazyVerticalGrid(
                modifier = Modifier.fillMaxSize(),
                columns = GridCells.Adaptive(128.dp),
                contentPadding = PaddingValues(vertical = 5.dp, horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
                horizontalArrangement = Arrangement.spacedBy(
                    16.dp,
                    Alignment.CenterHorizontally,
                ),
            ) {
                items(state.programStages, key = { it.uid!! }) {
                    Text(it.displayName!!)
                    ActionCard(
                        modifier = Modifier.fillMaxWidth(),
                        label = it.displayName,
                        enabled = state.filterState.filterDetailsState.count != 0,
                        icon = painterResource(ModuleIcons.getModuleIconByName(TERMS)),
                        onClick = {

                        }
                    )
                }
            }
        } else {
            ConfigNotFound(Modifier.fillMaxWidth().padding(horizontal = 16.dp), message = stringResource(
                R.string.performance_config_error))
        }
    }
}