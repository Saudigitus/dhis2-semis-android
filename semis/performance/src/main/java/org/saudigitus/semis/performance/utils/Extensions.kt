package org.saudigitus.semis.performance.utils

import org.hisp.dhis.android.core.program.ProgramStage
import org.saudigitus.semis.performance.models.ProgramStageModel

fun List<ProgramStage>.toProgramStageModel(): List<ProgramStageModel> {
    return this.map {
        ProgramStageModel(
            displayName = it.displayName(),
            uid = it.uid()
        )
    }
}