package org.saudigitus.semis.core.designsystem.components.summary

import org.saudigitus.semis.core.designsystem.components.FilterDetailsState
import org.saudigitus.semis.core.designsystem.components.bottomsheet.model.BottomSheetModel

data class SummaryState(
    val filterDetailsState: FilterDetailsState = FilterDetailsState(),
    val bottomSheetModels: List<BottomSheetModel> = emptyList(),
    val enableBulk: Boolean = false,
)