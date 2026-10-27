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
) {

    /**
     * Whether the user has told the app which class they are working on.
     *
     * This is what opens the modules, not how many learners the class already holds. A class that
     * has none still has to be reachable, otherwise the first learner could never be enrolled into
     * it, which offline leaves no way out at all.
     */
    val areModulesAvailable: Boolean
        get() = filterState.isFilterSelectionNotEmpty()

    /** What the screen has to tell the user about the class they picked, if anything. */
    val notice: HomeNotice
        get() = when {
            !areModulesAvailable -> HomeNotice.SELECT_FILTERS
            filterState.filterDetailsState.count == 0 -> HomeNotice.NO_DATA
            else -> HomeNotice.NONE
        }
}
