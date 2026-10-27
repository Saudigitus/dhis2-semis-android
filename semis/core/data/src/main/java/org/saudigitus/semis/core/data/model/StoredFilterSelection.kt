package org.saudigitus.semis.core.data.model

/**
 * The class a user was last working on, kept as identifiers alone.
 *
 * Only codes and uids are remembered, never the labels that go with them: a school can be renamed
 * on the server, and a remembered name would then contradict what the app shows. What is read back
 * is a set of candidates, not a selection. Nothing here is applied before it has been found in the
 * data available at that moment.
 *
 * [filterCodes] is keyed by the name of the filter type, so that this module does not have to know
 * about the type that describes filters on screen.
 */
data class StoredFilterSelection(
    val orgUnitUid: String? = null,
    val academicYearCode: String? = null,
    val filterCodes: Map<String, String> = emptyMap(),
) {

    /** Whether anything at all was remembered, so a first run can be told apart from an empty one. */
    val isEmpty: Boolean
        get() = orgUnitUid == null && academicYearCode == null && filterCodes.isEmpty()
}
