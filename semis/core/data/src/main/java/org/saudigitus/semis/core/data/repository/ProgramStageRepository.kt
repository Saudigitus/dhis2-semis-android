package org.saudigitus.semis.core.data.repository

import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.android.core.program.ProgramStageDataElement
import org.saudigitus.semis.core.data.model.app_config.ProgramStages

interface ProgramStageRepository {
    suspend fun getProgramStageDataElements(
        program: String,
        stage: String,
        dl: String? = null,
    ): List<ProgramStageDataElement>

    suspend fun getProgramStagesByIds(
        programStagesIds: List<ProgramStages>,
        ): List<ProgramStage>
}
