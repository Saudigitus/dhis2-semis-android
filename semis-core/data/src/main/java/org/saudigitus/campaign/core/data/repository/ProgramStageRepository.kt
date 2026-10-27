package org.saudigitus.campaign.core.data.repository

import org.saudigitus.campaign.core.data.models.ProgramStageDataElementModel
import org.saudigitus.campaign.core.data.models.ProgramStageModel
import org.saudigitus.campaign.core.data.models.ProgramStageSectionModel

interface ProgramStageRepository {
    suspend fun getProgramStageDataElements(
        stage: String,
        dl: String? = null,
    ): List<ProgramStageDataElementModel>

    suspend fun getProgramStageSectionsWithDataElements(
        vararg stages: String,
    ): List<ProgramStageSectionModel>

    suspend fun getActiveProgramStages(program: String): List<ProgramStageModel>
}
