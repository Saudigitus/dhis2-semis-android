package org.saudigitus.semis.app.presentation.home.model

import org.saudigitus.semis.core.data.model.OrgUnit
import org.saudigitus.semis.core.data.model.StoredFilterSelection
import org.saudigitus.semis.core.designsystem.components.fields.DropdownState
import org.saudigitus.semis.core.designsystem.components.model.DropdownItem
import org.saudigitus.semis.core.designsystem.components.model.FilterType

/**
 * Finds the school that was remembered among the ones the user may still capture into.
 *
 * A school can leave a user's scope between two openings, and what was remembered then names
 * something the picker would no longer offer. Returning nothing in that case is what keeps the
 * restored selection to choices the user could have made by hand.
 */
internal fun restoredOrgUnit(
    stored: StoredFilterSelection,
    available: List<OrgUnit>,
): OrgUnit? {
    val uid = stored.orgUnitUid ?: return null
    return available.firstOrNull { it.uid == uid }
}

/**
 * Rebuilds the chosen values from what was remembered, keeping only what still exists.
 *
 * Every code is looked up in the list the app has just loaded, so a value that has disappeared from
 * a class, or an academic year that has left the calendar, is simply not restored. The filters that
 * describe a class only make sense under a school, so when [orgUnitRestored] is false they are
 * dropped as a whole: half a class is a worse answer than none, since it would look like a
 * deliberate choice. The academic year does not depend on the school and is restored either way,
 * and when it cannot be, the caller's own default stands.
 */
internal fun restoredSelectedFilters(
    stored: StoredFilterSelection,
    orgUnitRestored: Boolean,
    academicYearOptions: List<DropdownItem>,
    filters: List<DropdownState>,
): Map<FilterType, DropdownItem> = buildMap {
    academicYearOptions.findByCode(stored.academicYearCode)?.let {
        put(FilterType.ACADEMIC_YEAR, it)
    }

    if (!orgUnitRestored) return@buildMap

    filters.forEach { filter ->
        val code = stored.filterCodes[filter.filterType.name] ?: return@forEach
        filter.data.findByCode(code)?.let { put(filter.filterType, it) }
    }
}

/**
 * The school when it is the only one the user may capture into.
 *
 * A user attached to a single school is asked, at every opening, to choose between one option.
 * Where there is one possibility there is no decision to take, so the app takes it. Where there is
 * more than one, or none at all, it takes nothing.
 */
internal fun onlyOrgUnit(available: List<OrgUnit>): OrgUnit? = available.singleOrNull()

/**
 * Fills in the filters that offer a single value and have not been chosen yet.
 *
 * The same reasoning as for the school: one possibility is not a choice. A filter that offers
 * nothing is left alone rather than resolved to an empty value, and a filter the user or the
 * remembered selection already answered is never overwritten.
 *
 * [excluded] carries the filters that must not be resolved this way. It is not an oversight that
 * some are excluded: the academic year already arrives chosen from configuration, and a second rule
 * competing for it would make the outcome depend on ordering, while the section is served by a list
 * that is loaded before any school is known and never reloaded, so what it offers cannot be trusted
 * enough to choose from without the user looking at it.
 */
internal fun autoSelectedFilters(
    filters: List<DropdownState>,
    selected: Map<FilterType, DropdownItem>,
    excluded: Set<FilterType>,
): Map<FilterType, DropdownItem> = buildMap {
    filters.forEach { filter ->
        if (filter.filterType in excluded) return@forEach
        if (selected.containsKey(filter.filterType)) return@forEach

        filter.data.singleOrNull()?.let { put(filter.filterType, it) }
    }
}

/** Reduces the chosen values back to the identifiers worth remembering. */
internal fun storedFilterSelection(
    orgUnit: OrgUnit?,
    selectedFilters: Map<FilterType, DropdownItem>,
): StoredFilterSelection = StoredFilterSelection(
    orgUnitUid = orgUnit?.uid,
    academicYearCode = selectedFilters[FilterType.ACADEMIC_YEAR]?.code,
    filterCodes = selectedFilters
        .filterKeys { it != FilterType.ACADEMIC_YEAR }
        .mapNotNull { (type, item) -> item.code?.let { type.name to it } }
        .toMap(),
)

private fun List<DropdownItem>.findByCode(code: String?): DropdownItem? {
    val wanted = code?.takeIf { it.isNotBlank() } ?: return null
    return firstOrNull { it.code == wanted }
}
