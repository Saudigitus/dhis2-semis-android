package org.saudigitus.semis.core.designsystem.utils

import org.saudigitus.semis.core.designsystem.R
import org.saudigitus.semis.core.designsystem.components.fields.DropdownState
import org.saudigitus.semis.core.designsystem.components.model.FilterType

fun DropdownState.icon() = when (this.filterType) {
    FilterType.ACADEMIC_YEAR -> R.drawable.ic_book
    FilterType.GRADE -> R.drawable.ic_school
    FilterType.SECTION -> R.drawable.ic_category
    FilterType.SCHOOL -> R.drawable.ic_location_on
    FilterType.UNKNOWN -> R.drawable.filter_none
}

fun DropdownState.placeholder() = when (this.filterType) {
    FilterType.ACADEMIC_YEAR -> R.string.academic_year
    FilterType.GRADE -> R.string.grade
    FilterType.SECTION -> R.string.cls
    FilterType.SCHOOL -> R.string.school
    FilterType.UNKNOWN -> R.string.unknown
}