package org.saudigitus.semis.app.presentation.home

import org.saudigitus.semis.app.presentation.model.Module
import org.saudigitus.semis.core.designsystem.components.model.ToolbarHeaders
import org.saudigitus.semis.core.designsystem.filters.FilterComponentState

data class HomeUIState(
    val isLoading: Boolean = false,
    val toolbarHeaders: ToolbarHeaders = ToolbarHeaders(title = "Home"),
    val displayFilters: Boolean = true,
    val program: String = "",
    val filterState: FilterComponentState = FilterComponentState(),
    val modules: List<Module> = emptyList(),
)
