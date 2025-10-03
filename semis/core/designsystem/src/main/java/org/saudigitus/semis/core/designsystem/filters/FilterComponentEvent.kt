package org.saudigitus.semis.core.designsystem.filters

import org.saudigitus.semis.core.designsystem.components.model.FilterType

sealed interface FilterComponentEvent {
    data object Sync : FilterComponentEvent
    data class FilterValueChange<T>(val filterType: FilterType, val obj: T) : FilterComponentEvent
}