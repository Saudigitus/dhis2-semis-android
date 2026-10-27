package org.saudigitus.semis.core.data.model.app_config

import org.saudigitus.semis.core.utils.Constants

/**
 * The data element the grade filter of the program selects on, read from the filter itself, as
 * the web does. The registration grade is only the fallback for a program that defines no grade
 * filter, so the two settings can disagree without the home losing its options or its records.
 */
fun SEMISConfigItem.gradeFilterDataElement(): String? =
    filterDataElement(GRADE_CODES) ?: registration?.grade?.takeIf { it.isNotBlank() }

/** The data element the class or section filter selects on, with the same fallback. */
fun SEMISConfigItem.sectionFilterDataElement(): String? =
    filterDataElement(SECTION_CODES) ?: registration?.section?.takeIf { it.isNotBlank() }

private fun SEMISConfigItem.filterDataElement(codes: Set<String>): String? =
    filters?.dataElements
        ?.firstOrNull { it?.code in codes }
        ?.dataElement
        ?.takeIf { it.isNotBlank() }

private val GRADE_CODES = setOf(Constants.GRADE)
private val SECTION_CODES = setOf(Constants.SECTION, Constants.SECTION_CLASS, Constants.CLASS)
