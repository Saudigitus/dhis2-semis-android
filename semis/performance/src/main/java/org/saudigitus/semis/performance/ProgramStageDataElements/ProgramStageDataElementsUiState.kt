package org.saudigitus.semis.performance.ProgramStageDataElements

import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.program.ProgramStageDataElement
import org.saudigitus.semis.core.designsystem.components.model.ToolbarHeaders
import org.saudigitus.semis.core.designsystem.filters.FilterComponentState
import org.saudigitus.semis.performance.models.ProgramStageModel

data class ProgramStageDataElementsUiState (
    val isLoading: Boolean = false,
    val toolbarHeaders: ToolbarHeaders = ToolbarHeaders(title = ""),
    val filterState: FilterComponentState = FilterComponentState(enableCounter = false),
    val programStageDataElements: List<DataElement> = emptyList(),
)