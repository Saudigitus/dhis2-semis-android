package org.saudigitus.semis.core.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.program.ProgramStage
import org.saudigitus.semis.core.data.model.app_config.ProgramStages
import javax.inject.Inject

class ProgramStageRepositoryImpl @Inject constructor(
    val d2: D2
) : ProgramStageRepository {
    override suspend fun getProgramStageDataElements(
        program: String,
        stage: String,
        dl: String?
    ) = withContext(Dispatchers.IO) {
        val repository = d2.programModule().programStageDataElements()
            .byProgramStage().eq(stage)

        if (dl != null) {
            repository.byDataElement().eq(dl)
                .blockingGet()
        } else {
            repository.blockingGet()
        }
    }

    override suspend fun getProgramStagesByIds(programStagesIds: List<ProgramStages>) =
        withContext(Dispatchers.IO) {
            val ids = programStagesIds.mapNotNull { it.programStage }
            return@withContext d2.programModule().programStages().byUid()
                .`in`(ids)
                .blockingGet()
        }
}