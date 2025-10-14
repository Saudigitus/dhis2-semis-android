package org.saudigitus.semis.app.presentation.home

import androidx.compose.runtime.Immutable
import org.saudigitus.semis.core.data.model.Module
import org.saudigitus.semis.core.data.model.SearchTeiModel
import org.saudigitus.semis.core.designsystem.components.model.ToolbarHeaders
import org.saudigitus.semis.core.designsystem.filters.FilterComponentState

@Immutable
data class HomeUIState(
    val isLoading: Boolean = false,
    val toolbarHeaders: ToolbarHeaders = ToolbarHeaders(title = "Home"),
    val displayFilters: Boolean = true,
    val program: String = "",
    val programName: String = "",
    val filterState: FilterComponentState = FilterComponentState(),
    val modules: List<Module> = emptyList(),
    val tei: List<SearchTeiModel> = emptyList(),
    val errorMessage: String? = null
)
