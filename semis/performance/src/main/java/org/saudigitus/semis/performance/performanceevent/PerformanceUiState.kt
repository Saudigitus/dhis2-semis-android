package org.saudigitus.semis.performance.performanceevent

import org.saudigitus.semis.core.data.model.SearchTeiModel
import org.saudigitus.semis.core.designsystem.components.model.ToolbarHeaders
import org.saudigitus.semis.core.designsystem.components.summary.SummaryState
import org.saudigitus.semis.core.form.ui.state.FormBuilderState
import org.saudigitus.semis.core.utils.ButtonStep

data class PerformanceUiState(
    val isLoading: Boolean = false,
    val program: String = "",
    val tei: List<SearchTeiModel> = emptyList(),
    val toolbarHeaders: ToolbarHeaders = ToolbarHeaders(""),
    val formBuilderState: FormBuilderState = FormBuilderState(),
    val summaryState: SummaryState = SummaryState(),
    val buttonStep: ButtonStep = ButtonStep.NONE,
    val isConfirmDialog: Boolean = false,
    val errorMessage: String? = null
)