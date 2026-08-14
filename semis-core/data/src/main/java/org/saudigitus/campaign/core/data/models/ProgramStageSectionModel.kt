package org.saudigitus.campaign.core.data.models

import org.hisp.dhis.android.core.program.SectionRendering

data class ProgramStageSectionModel(
    val uid: String,
    val programStageUid: String? = null,
    val code: String? = null,
    val displayName: String?,
    val description: String? = null,
    val programStageDataElements: List<ProgramStageDataElementModel> = emptyList(),
    val renderType: SectionRendering? = null,
    val sortOrder: Int? = -1
)
