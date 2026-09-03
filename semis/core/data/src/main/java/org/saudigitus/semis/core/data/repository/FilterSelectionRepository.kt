package org.saudigitus.semis.core.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.dhis2.commons.prefs.PreferenceProvider
import org.hisp.dhis.android.core.D2
import org.saudigitus.semis.core.data.model.StoredFilterSelection

/**
 * Remembers the class a user was working on, so that opening a module does not start from nothing.
 */
interface FilterSelectionRepository {

    /** What was remembered for this program, empty when there is nothing or it cannot be read. */
    suspend fun read(program: String): StoredFilterSelection

    /** Records the current selection, replacing whatever was remembered before. */
    suspend fun save(program: String, selection: StoredFilterSelection)
}

class FilterSelectionRepositoryImpl(
    private val d2: D2,
    private val preferences: PreferenceProvider,
) : FilterSelectionRepository {

    override suspend fun read(program: String): StoredFilterSelection = withContext(Dispatchers.IO) {
        val prefix = keyPrefix(program)

        StoredFilterSelection(
            orgUnitUid = preferences.getString("$prefix$ORG_UNIT")?.takeIf { it.isNotBlank() },
            academicYearCode = preferences.getString("$prefix$ACADEMIC_YEAR")?.takeIf { it.isNotBlank() },
            filterCodes = preferences.getString("$prefix$FILTERS").orEmpty().toFilterCodes(),
        )
    }

    override suspend fun save(program: String, selection: StoredFilterSelection) = withContext(Dispatchers.IO) {
        val prefix = keyPrefix(program)

        preferences.setValue("$prefix$ORG_UNIT", selection.orgUnitUid.orEmpty())
        preferences.setValue("$prefix$ACADEMIC_YEAR", selection.academicYearCode.orEmpty())
        preferences.setValue("$prefix$FILTERS", selection.filterCodes.toStoredText())
    }

    /**
     * Ties what is remembered to the server, the user and the program it was chosen under.
     *
     * These preferences are cleared when local data is deleted but not when a user logs out, so
     * without the server and the user in the key a selection could reappear for somebody who never
     * made it. When either cannot be read the value is left blank, which keeps the key stable and
     * still separates one program from another.
     */
    private fun keyPrefix(program: String): String {
        val server = d2.systemInfoModule().systemInfo().blockingGet()?.contextPath().orEmpty()
        val user = d2.userModule().authenticatedUser().blockingGet()?.user().orEmpty()

        return "$KEY_ROOT$server|$user|$program|"
    }

    /**
     * The filters are held as one entry rather than one per filter, because the set of filters is
     * configured per server and can change; a single entry is replaced whole and never leaves
     * behind a key for a filter that no longer exists.
     */
    private fun Map<String, String>.toStoredText() = entries
        .filter { it.key.isNotBlank() && it.value.isNotBlank() }
        .joinToString(ENTRY_SEPARATOR) { "${it.key}$PAIR_SEPARATOR${it.value}" }

    private fun String.toFilterCodes(): Map<String, String> = split(ENTRY_SEPARATOR)
        .mapNotNull { entry ->
            val parts = entry.split(PAIR_SEPARATOR)
            if (parts.size == 2 && parts[0].isNotBlank() && parts[1].isNotBlank()) {
                parts[0] to parts[1]
            } else {
                null
            }
        }
        .toMap()

    private companion object {
        const val KEY_ROOT = "semis_filter_selection|"
        const val ORG_UNIT = "org_unit"
        const val ACADEMIC_YEAR = "academic_year"
        const val FILTERS = "filters"
        const val ENTRY_SEPARATOR = ";"
        const val PAIR_SEPARATOR = "="
    }
}
