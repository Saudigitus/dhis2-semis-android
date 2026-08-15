package org.saudigitus.campaign.core.data.repository

import org.hisp.dhis.android.core.option.Option
import org.hisp.dhis.android.core.program.Program
import org.saudigitus.campaign.core.data.models.FormFieldEntity
import org.saudigitus.campaign.core.data.models.TrackedEntityAttributeModel
import org.saudigitus.campaign.core.data.models.TrackedEntityAttributeSectionModel

interface EnrollmentRepository {
    suspend fun create(orgUnit: String, program: String, date: String, fields: List<FormFieldEntity>): Pair<String, String>
    suspend fun update(enrollment: String, tei: String, date: String, fields: List<FormFieldEntity>): Pair<String, String>
    suspend fun getEnrollmentDate(tei: String?): Long?
}

interface OptionRepository {
    suspend fun getOptions(optionSetUid: String): List<Option>
    suspend fun getOptionsByDataElement(dataElement: String): List<Option>
    suspend fun getOptionsByCode(dataElement: String, codes: List<String>): List<Option>
}

interface ProgramRepository {
    suspend fun getTrackedEntityAttribute(program: String, searchable: Boolean = false): List<TrackedEntityAttributeModel>
    suspend fun getTrackedEntityAttributeWithSection(program: String): List<TrackedEntityAttributeSectionModel>
    suspend fun getPrograms(): List<Program>
    suspend fun getPrograms(programs: Map<String?, Int?>): List<Program>
    suspend fun hasPermissionToWrite(program: String): Boolean
    suspend fun hasPermissionToWrite(program: String, programStage: String): Boolean
}

interface TeiRepository {
    suspend fun create(orgUnit: String, program: String, fields: List<FormFieldEntity>): String?
    suspend fun update(tei: String, fields: List<FormFieldEntity>): String?
}
