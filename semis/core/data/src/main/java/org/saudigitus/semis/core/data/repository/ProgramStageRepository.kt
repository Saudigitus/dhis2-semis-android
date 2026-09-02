package org.saudigitus.semis.core.data.repository

import org.hisp.dhis.android.core.program.ProgramStage
import org.saudigitus.semis.core.data.model.ProgramStageDataElementModel
import org.saudigitus.semis.core.data.model.app_config.ProgramStages

interface ProgramStageRepository {
    suspend fun getProgramStageDataElements(
        stage: String,
        dl: String? = null,
    ): List<ProgramStageDataElementModel>

    suspend fun getProgramStagesByIds(
        programStagesIds: List<ProgramStages>,
        ): List<ProgramStage>
}
