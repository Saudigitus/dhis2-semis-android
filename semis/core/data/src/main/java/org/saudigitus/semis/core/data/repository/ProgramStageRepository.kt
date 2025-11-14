package org.saudigitus.semis.core.data.repository

import org.hisp.dhis.android.core.program.ProgramStageDataElement

interface ProgramStageRepository {
    suspend fun getProgramStageDataElements(
        program: String,
        stage: String,
        dl: String? = null,
    ): List<ProgramStageDataElement>
}
