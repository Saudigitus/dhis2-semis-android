package org.saudigitus.campaign.core.data.repository.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.dhis2.commons.resources.ResourceManager
import org.hisp.dhis.android.core.D2
import org.saudigitus.campaign.core.data.R
import org.saudigitus.campaign.core.data.models.ProgramStageDataElementModel
import org.saudigitus.campaign.core.data.models.ProgramStageModel
import org.saudigitus.campaign.core.data.models.ProgramStageSectionModel
import org.saudigitus.campaign.core.data.repository.ProgramStageRepository
import org.saudigitus.campaign.core.utils.IdGenerator
import javax.inject.Inject

class ProgramStageRepositoryImpl @Inject constructor(
    val d2: D2,
    private val resourceManager: ResourceManager,
) : ProgramStageRepository {
    override suspend fun getProgramStageDataElements(
        stage: String,
        dl: String?
    ) = withContext(Dispatchers.IO) {
        val repository = d2.programModule().programStageDataElements()
            .byProgramStage().eq(stage)

        if (dl != null) {
            repository.byDataElement().eq(dl)
                .blockingGet().map {
                    val dataElement =
                        d2.dataElementModule().dataElements().uid(it.dataElement()?.uid())
                            .blockingGet()

                    ProgramStageDataElementModel(
                        programStageUid = it.programStage()?.uid(),
                        code = it.code(),
                        displayName = it.displayName(),
                        dataElement = dataElement,
                        compulsory = it.compulsory(),
                        renderType = it.renderType(),
                        allowFutureDate = it.allowFutureDate(),
                        sortOrder = it.sortOrder()
                    )
                }
        } else {
            repository.blockingGet().map {
                val dataElement =
                    d2.dataElementModule().dataElements().uid(it.dataElement()?.uid())
                        .blockingGet()

                ProgramStageDataElementModel(
                    programStageUid = it.programStage()?.uid(),
                    code = it.code(),
                    displayName = it.displayName(),
                    dataElement = dataElement,
                    compulsory = it.compulsory(),
                    renderType = it.renderType(),
                    allowFutureDate = it.allowFutureDate(),
                    sortOrder = it.sortOrder()
                )
            }
        }
    }

    override suspend fun getActiveProgramStages(program: String): List<ProgramStageModel> =
        withContext(Dispatchers.IO) {
            val registrations: List<ProgramStageModel> =
                d2.programModule()
                    .programStages()
                    .byProgramUid().eq(program)
                    .byAccessDataWrite()
                    .isTrue
                    .blockingGet()
                    .map { programStage ->
                        ProgramStageModel(
                            displayName = programStage.displayName().orEmpty(),
                            uid = programStage.uid().orEmpty(),
                            repeatable = programStage.repeatable() == true
                        )
                    }
            registrations
        }

    override suspend fun getProgramStageSectionsWithDataElements(vararg stages: String) =
        withContext(Dispatchers.IO) {
            val data = d2.programModule()
                .programStageSections()
                .byProgramStageUid()
                .`in`(stages.toList())
                .withDataElements()
                .blockingGet()

            if (data.isNotEmpty()) {
                data.map {
                    val psDataElements = it.dataElements()?.flatMap { dataElement ->
                        getProgramStageDataElements(
                            it.programStage()?.uid().orEmpty(),
                            dataElement.uid()
                        )
                    } ?: emptyList()

                    ProgramStageSectionModel(
                        uid = it.uid(),
                        programStageUid = it.programStage()?.uid(),
                        code = it.code(),
                        displayName = it.displayName(),
                        description = it.displayDescription(),
                        programStageDataElements = psDataElements,
                        renderType = it.renderType(),
                        sortOrder = it.sortOrder()
                    )
                }.sortedBy { it.sortOrder }
            } else {
                stages.map { stage ->
                    val psDataElements = getProgramStageDataElements(stage)

                    ProgramStageSectionModel(
                        uid = IdGenerator.generateDhis2PatternId(),
                        displayName = resourceManager.getString(R.string.form_title),
                        programStageDataElements = psDataElements,
                    )
                }
            }
        }

}