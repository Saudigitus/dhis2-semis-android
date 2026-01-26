package org.saudigitus.semis.performance.programstagedataelement

import org.saudigitus.semis.core.data.model.ProgramStageDataElementModel
import org.saudigitus.semis.core.designsystem.components.model.ToolbarHeaders
import org.saudigitus.semis.core.designsystem.filters.FilterComponentState

data class ProgramStageDataElementsUiState (
    val isLoading: Boolean = false,
    val toolbarHeaders: ToolbarHeaders = ToolbarHeaders(title = ""),
    val filterState: FilterComponentState = FilterComponentState(enableCounter = false),
    val programStageDataElements: List<ProgramStageDataElementModel> = emptyList(),
)