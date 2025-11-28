package org.saudigitus.semis.core.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.hisp.dhis.android.core.D2
import javax.inject.Inject

class ProgramStageRepositoryImpl @Inject constructor(
    val d2: D2
): ProgramStageRepository {
    override suspend fun getProgramStageDataElements(
        program: String,
        stage: String,
        dl: String?
    ) =  withContext(Dispatchers.IO) {
        val repository = d2.programModule().programStageDataElements()
            .byProgramStage().eq(stage)

        if (dl != null) {
            repository.byDataElement().eq(dl)
                .blockingGet()
        } else {
            repository.blockingGet()
        }
    }
}