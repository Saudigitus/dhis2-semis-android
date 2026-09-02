package org.saudigitus.semis.core.designsystem.utils

import org.saudigitus.semis.core.data.model.OrgUnit
import org.saudigitus.semis.core.designsystem.components.FilterDetailsState
import org.saudigitus.semis.core.designsystem.components.fields.DropdownState
import org.saudigitus.semis.core.designsystem.components.model.DropdownItem
import org.saudigitus.semis.core.designsystem.components.model.FilterType
import org.saudigitus.semis.core.designsystem.components.model.ToolbarHeaders
import org.saudigitus.semis.core.designsystem.filters.FilterComponentState

fun FilterComponentState.withSelectedFilter(
    type: FilterType,
    item: DropdownItem
) = copy(selectedFilters = selectedFilters + (type to item))

fun FilterComponentState.withOUAndFilters(
    orgUnit: OrgUnit,
    filters: List<DropdownState>
) = copy(orgUnit = orgUnit, filters = filters)

fun FilterComponentState.withFilterDetails(
    filterDetailsState: FilterDetailsState
) = copy(filterDetailsState = filterDetailsState)

fun ToolbarHeaders.withSubtitle(subtitle: String) = copy(subtitle = subtitle)