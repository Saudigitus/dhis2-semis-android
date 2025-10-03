package org.saudigitus.semis.core.designsystem.utils

import org.saudigitus.semis.core.designsystem.components.model.FilterType
import org.saudigitus.semis.core.utils.Constants

object UiDefaults {
    val filterTypeMap = mapOf(
        Constants.GRADE to FilterType.GRADE,
        Constants.SECTION to FilterType.SECTION,
        Constants.SECTION_CLASS to FilterType.SECTION,
        Constants.CLASS to FilterType.SECTION
    )
}