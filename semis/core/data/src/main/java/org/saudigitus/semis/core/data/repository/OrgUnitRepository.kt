package org.saudigitus.semis.core.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.dhis2.commons.orgunitselector.OURepositoryConfiguration
import org.dhis2.commons.orgunitselector.OrgUnitSelectorScope
import org.hisp.dhis.android.core.D2
import org.saudigitus.semis.core.data.model.OrgUnit

/**
 * The schools a user may capture into for a program.
 */
interface OrgUnitRepository {

    /**
     * The schools the organisation unit picker would offer for this program.
     *
     * Anything that decides on a school without the user, whether restoring an earlier choice or
     * resolving one that is the only possibility, has to agree with what the picker shows. Reading
     * the same list is what makes that agreement hold instead of being maintained by hand.
     */
    suspend fun captureOrgUnits(program: String): List<OrgUnit>
}

class OrgUnitRepositoryImpl(
    private val d2: D2,
) : OrgUnitRepository {

    override suspend fun captureOrgUnits(program: String): List<OrgUnit> = withContext(Dispatchers.IO) {
        OURepositoryConfiguration(d2, OrgUnitSelectorScope.ProgramCaptureScope(program))
            .orgUnitRepository(null)
            .map { OrgUnit(uid = it.uid(), displayName = it.displayName()) }
    }
}
