package org.saudigitus.semis.performance.programstage

import androidx.compose.runtime.Immutable
import org.saudigitus.semis.core.designsystem.components.model.ToolbarHeaders
import org.saudigitus.semis.core.designsystem.filters.FilterComponentState
import org.saudigitus.semis.performance.models.ProgramStageModel

@Immutable
data class ProgramStageUiState (
    val isLoading: Boolean = false,
    val toolbarHeaders: ToolbarHeaders = ToolbarHeaders(title = ""),
    val program: String = "",
    val filterState: FilterComponentState = FilterComponentState(),
    val programStages: List<ProgramStageModel> = emptyList(),
)