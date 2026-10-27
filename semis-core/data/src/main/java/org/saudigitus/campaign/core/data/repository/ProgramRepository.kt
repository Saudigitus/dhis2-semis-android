package org.saudigitus.campaign.core.data.repository

import org.hisp.dhis.android.core.program.Program
import org.saudigitus.campaign.core.data.models.TrackedEntityAttributeModel
import org.saudigitus.campaign.core.data.models.TrackedEntityAttributeSectionModel

interface ProgramRepository {
    suspend fun getTrackedEntityAttribute(program: String, searchable: Boolean = false): List<TrackedEntityAttributeModel>
    suspend fun getTrackedEntityAttributeWithSection(program: String): List<TrackedEntityAttributeSectionModel>
    suspend fun getPrograms(): List<Program>
    suspend fun getPrograms(programs: Map<String?, Int?>): List<Program>

    suspend fun hasPermissionToWrite(program: String): Boolean
    suspend fun hasPermissionToWrite(program: String, programStage: String): Boolean
}