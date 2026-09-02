package org.saudigitus.semis.core.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.hisp.dhis.android.core.D2
import org.saudigitus.semis.core.data.model.ProgramStageDataElementModel
import org.saudigitus.semis.core.data.model.app_config.ProgramStages
import javax.inject.Inject

class ProgramStageRepositoryImpl @Inject constructor(
    val d2: D2
) : ProgramStageRepository {
    override suspend fun getProgramStageDataElements(
        stage: String,
        dl: String?
    ) = withContext(Dispatchers.IO) {
        var repository = d2.programModule().programStageDataElements()
            .byProgramStage().eq(stage)
        dl?.let { repository = repository.byDataElement().eq(it) }

        val stageDataElements = repository.blockingGet()
        val dataElementUids = stageDataElements
            .mapNotNull { it.dataElement()?.uid() }
            .distinct()
        val dataElementsByUid = if (dataElementUids.isEmpty()) {
            emptyMap()
        } else {
            d2.dataElementModule().dataElements()
                .byUid().`in`(dataElementUids)
                .blockingGet()
                .associateBy { it.uid() }
        }

        stageDataElements.map {
            ProgramStageDataElementModel(
                programStageUid = it.programStage()?.uid(),
                code = it.code(),
                displayName = it.displayName(),
                dataElement = dataElementsByUid[it.dataElement()?.uid()],
                compulsory = it.compulsory(),
                renderType = it.renderType(),
                allowFutureDate = it.allowFutureDate(),
                sortOrder = it.sortOrder(),
            )
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
