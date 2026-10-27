package org.saudigitus.semis.core.designsystem.components.summary

import androidx.compose.runtime.Stable
import org.saudigitus.semis.core.designsystem.components.FilterDetailsState
import org.saudigitus.semis.core.designsystem.components.bottomsheet.model.BottomSheetModel

@Stable
data class SummaryState(
    val filterDetailsState: FilterDetailsState = FilterDetailsState(),
    val bottomSheetModels: List<BottomSheetModel> = emptyList(),
    val enableBulk: Boolean = false,
)