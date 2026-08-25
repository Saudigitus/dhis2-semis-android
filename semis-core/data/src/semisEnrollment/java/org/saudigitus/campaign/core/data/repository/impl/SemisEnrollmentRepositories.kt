package org.saudigitus.campaign.core.data.repository.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.enrollment.EnrollmentCreateProjection
import org.hisp.dhis.android.core.option.Option
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceCreateProjection
import org.saudigitus.campaign.core.data.models.FormFieldEntity
import org.saudigitus.campaign.core.data.models.TrackedEntityAttributeModel
import org.saudigitus.campaign.core.data.models.TrackedEntityAttributeSectionModel
import org.saudigitus.campaign.core.data.repository.EnrollmentRepository
import org.saudigitus.campaign.core.data.repository.OptionRepository
import org.saudigitus.campaign.core.data.repository.ProgramRepository
import org.saudigitus.campaign.core.data.repository.TeiRepository
import java.sql.Date
import java.util.UUID

class SemisEnrollmentOptionRepository(private val d2: D2) : OptionRepository {
    override suspend fun getOptions(optionSetUid: String): List<Option> = withContext(Dispatchers.IO) {
        d2.optionModule().options().byOptionSetUid().eq(optionSetUid)
            .orderBySortOrder(RepositoryScope.OrderByDirection.ASC).blockingGet()
    }
    override suspend fun getOptionsByDataElement(dataElement: String) = emptyList<Option>()
    override suspend fun getOptionsByCode(dataElement: String, codes: List<String>) = emptyList<Option>()
}

class SemisEnrollmentTeiRepository(
    private val d2: D2,
) : TeiRepository {
    override suspend fun create(orgUnit: String, program: String, fields: List<FormFieldEntity>): String = withContext(Dispatchers.IO) {
        val trackedEntityType = d2.programModule().programs().uid(program).blockingGet()
            ?.trackedEntityType()?.uid()
            ?: throw IllegalStateException("Tracked entity type not found for program $program")
        val tei = d2.trackedEntityModule().trackedEntityInstances().blockingAdd(
            TrackedEntityInstanceCreateProjection.builder().organisationUnit(orgUnit)
                .trackedEntityType(trackedEntityType).build(),
        )
        fields.forEach { field ->
            d2.trackedEntityModule().trackedEntityAttributeValues().value(field.uid, tei).blockingSet(field.value)
        }
        tei
    }

    override suspend fun update(tei: String, fields: List<FormFieldEntity>): String = withContext(Dispatchers.IO) {
        fields.forEach { field ->
            d2.trackedEntityModule().trackedEntityAttributeValues().value(field.uid, tei).blockingSet(field.value)
        }
        tei
    }
}

class SemisEnrollmentRepository(
    private val d2: D2,
    private val teiRepository: TeiRepository,
) : EnrollmentRepository {
    override suspend fun create(orgUnit: String, program: String, date: String, fields: List<FormFieldEntity>): Pair<String, String> = withContext(Dispatchers.IO) {
        val tei = teiRepository.create(orgUnit, program, fields)
            ?: throw IllegalStateException("Unable to create tracked entity")
        val enrollment = d2.enrollmentModule().enrollments().blockingAdd(
            EnrollmentCreateProjection.builder().organisationUnit(orgUnit).program(program)
                .trackedEntityInstance(tei).build(),
        )
        d2.enrollmentModule().enrollments().uid(enrollment).setEnrollmentDate(Date.valueOf(date))
        d2.enrollmentModule().enrollments().uid(enrollment).setIncidentDate(Date.valueOf(date))
        enrollment to tei
    }

    override suspend fun update(enrollment: String, tei: String, date: String, fields: List<FormFieldEntity>): Pair<String, String> {
        teiRepository.update(tei, fields)
        return tei to enrollment
    }

    override suspend fun getEnrollmentDate(tei: String?): Long? = tei?.let {
        d2.enrollmentModule().enrollments().byTrackedEntityInstance().eq(it).blockingGet().lastOrNull()?.enrollmentDate()?.time
    }
}

class SemisEnrollmentProgramRepository(private val d2: D2) : ProgramRepository {
    override suspend fun getTrackedEntityAttribute(program: String, searchable: Boolean): List<TrackedEntityAttributeModel> = withContext(Dispatchers.IO) {
        var query = d2.programModule().programTrackedEntityAttributes().byProgram().eq(program)
        if (searchable) query = query.bySearchable().isTrue
        val programAttributes = query.blockingGet()
        val attributeUids = programAttributes.mapNotNull { it.trackedEntityAttribute()?.uid() }
        val attributesByUid = if (attributeUids.isEmpty()) {
            emptyMap()
        } else {
            d2.trackedEntityModule().trackedEntityAttributes()
                .byUid().`in`(attributeUids)
                .blockingGet()
                .associateBy { it.uid() }
        }

        programAttributes.mapNotNull { programAttribute ->
            val attributeUid = programAttribute.trackedEntityAttribute()?.uid()
                ?: return@mapNotNull null
            val attribute = attributesByUid[attributeUid] ?: return@mapNotNull null
            TrackedEntityAttributeModel(
                uid = attribute.uid(),
                displayFormName = attribute.displayFormName(),
                code = attribute.code(),
                optionSetUid = attribute.optionSet()?.uid(),
                valueType = attribute.valueType(),
                mandatory = programAttribute.mandatory() ?: false,
                generated = attribute.generated() ?: false,
            )
        }
    }

    override suspend fun getTrackedEntityAttributeWithSection(program: String): List<TrackedEntityAttributeSectionModel> = withContext(Dispatchers.IO) {
        val sections = d2.programModule().programSections().byProgramUid().eq(program).withAttributes().blockingGet()
        if (sections.isEmpty()) {
            return@withContext listOf(TrackedEntityAttributeSectionModel(
                uid = UUID.randomUUID().toString(), displayName = "Enrollment", attributes = getTrackedEntityAttribute(program),
            ))
        }
        val programAttributesByAttribute = d2.programModule().programTrackedEntityAttributes()
            .byProgram().eq(program)
            .blockingGet()
            .associateBy { it.trackedEntityAttribute()?.uid() }

        // The attributes linked to a section carry only what the link table holds, so the full
        // records are read separately to reach the flags the form depends on, such as generated.
        val sectionAttributeUids = sections.flatMap { section ->
            section.attributes().orEmpty().map { it.uid() }
        }.distinct()
        val attributesByUid = if (sectionAttributeUids.isEmpty()) {
            emptyMap()
        } else {
            d2.trackedEntityModule().trackedEntityAttributes()
                .byUid().`in`(sectionAttributeUids)
                .blockingGet()
                .associateBy { it.uid() }
        }

        sections.map { section ->
            TrackedEntityAttributeSectionModel(
                uid = section.uid(), code = section.code(), displayName = section.displayName(),
                description = section.description(), sortOrder = section.sortOrder(),
                attributes = section.attributes().orEmpty().mapNotNull { sectionAttribute ->
                    val programAttribute = programAttributesByAttribute[sectionAttribute.uid()]
                        ?: return@mapNotNull null
                    val attribute = attributesByUid[sectionAttribute.uid()] ?: sectionAttribute
                    TrackedEntityAttributeModel(
                        uid = attribute.uid(),
                        displayFormName = attribute.displayFormName(),
                        code = attribute.code(),
                        optionSetUid = attribute.optionSet()?.uid(),
                        valueType = attribute.valueType(),
                        mandatory = programAttribute.mandatory() ?: false,
                        generated = attribute.generated() ?: false,
                    )
                },
            )
        }.sortedBy { it.sortOrder }
    }

    override suspend fun getPrograms() = d2.programModule().programs().blockingGet()
    override suspend fun getPrograms(programs: Map<String?, Int?>) = d2.programModule().programs().byUid().`in`(programs.keys.filterNotNull()).blockingGet()
    override suspend fun hasPermissionToWrite(program: String) = true
    override suspend fun hasPermissionToWrite(program: String, programStage: String) = true
}
