package org.saudigitus.semis.core.designsystem.filters

import org.saudigitus.semis.core.designsystem.components.model.FilterType

sealed interface FilterComponentEvent {
    data object Sync : FilterComponentEvent

    /** Clears the class the user is on, including the one remembered from an earlier session. */
    data object ResetFilters : FilterComponentEvent
    data class FilterValueChange<T>(val filterType: FilterType, val obj: T) : FilterComponentEvent
}