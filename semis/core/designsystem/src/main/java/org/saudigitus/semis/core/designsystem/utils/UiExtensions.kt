package org.saudigitus.semis.core.designsystem.utils

import org.saudigitus.semis.core.data.model.OrgUnit
import org.saudigitus.semis.core.designsystem.components.model.DropdownItem
import org.saudigitus.semis.core.designsystem.components.model.FilterType
import org.saudigitus.semis.core.designsystem.components.model.ToolbarHeaders
import org.saudigitus.semis.core.designsystem.filters.FilterComponentState

fun FilterComponentState.withOrgUnit(orgUnit: OrgUnit) = copy(orgUnit = orgUnit)

fun FilterComponentState.withSelectedFilter(
    type: FilterType,
    item: DropdownItem
) = copy(selectedFilters = selectedFilters + (type to item))

fun ToolbarHeaders.withSubtitle(subtitle: String) = copy(subtitle = subtitle)