package org.saudigitus.campaign.core.data.repository.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.dhis2.commons.bindings.program
import org.dhis2.commons.bindings.programStage
import org.dhis2.commons.resources.ResourceManager
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.program.Program
import org.saudigitus.campaign.core.data.R
import org.saudigitus.campaign.core.data.models.TrackedEntityAttributeModel
import org.saudigitus.campaign.core.data.models.TrackedEntityAttributeSectionModel
import org.saudigitus.campaign.core.data.repository.ProgramRepository
import org.saudigitus.campaign.core.utils.IdGenerator

class ProgramRepositoryImpl(
    private val d2: D2,
    private val resourceManager: ResourceManager,
) : ProgramRepository {
    override suspend fun getTrackedEntityAttribute(
        program: String,
        searchable: Boolean
    ): List<TrackedEntityAttributeModel> = withContext(Dispatchers.IO) {
        var repository = d2.programModule()
            .programTrackedEntityAttributes()
            .byProgram().eq(program)

        repository = if (searchable) {
            repository.bySearchable().isTrue
        } else repository

        repository
            .blockingGet()
            .mapNotNull { programAttribute ->
                val attribute = d2.trackedEntityModule()
                    .trackedEntityAttributes()
                    .uid(programAttribute.trackedEntityAttribute()?.uid())
                    .blockingGet()

                if (attribute == null) return@mapNotNull null

                TrackedEntityAttributeModel(
                    uid = attribute.uid(),
                    displayFormName = attribute.displayFormName(),
                    code = attribute.code(),
                    optionSetUid = attribute.optionSet()?.uid(),
                    valueType = attribute.valueType(),
                    mandatory = programAttribute.mandatory() ?: false
                )
            }
    }

    override suspend fun getTrackedEntityAttributeWithSection(
        program: String
    ) = withContext(Dispatchers.IO) {
        val data = d2.programModule()
            .programSections()
            .byProgramUid().eq(program)
            .withAttributes()
            .blockingGet()

        return@withContext if (data.isNotEmpty()) {
            data.map {
                val trackerAttributes = it.attributes()?.mapNotNull { attribute ->
                    val attr = d2.programModule().programTrackedEntityAttributes()
                        .byProgram().eq(program)
                        .byTrackedEntityAttribute().eq(attribute.uid())
                        .one()
                        .blockingGet()

                    if (attr == null) return@mapNotNull null

                    TrackedEntityAttributeModel(
                        uid = attribute.uid(),
                        displayFormName = attribute.displayFormName(),
                        code = attribute.code(),
                        optionSetUid = attribute.optionSet()?.uid(),
                        valueType = attribute.valueType(),
                        mandatory = attr.mandatory() ?: false,
                    )
                } ?: emptyList()

                TrackedEntityAttributeSectionModel(
                    uid = it.uid(),
                    code = it.code(),
                    displayName = it.displayName(),
                    description = it.description(),
                    attributes = trackerAttributes,
                    sortOrder = it.sortOrder()
                )
            }.sortedBy { it.sortOrder }
        } else {
            val trackerAttributes = getTrackedEntityAttribute(program)

            buildList {
                add(
                    TrackedEntityAttributeSectionModel(
                        uid = IdGenerator.generateDhis2PatternId(),
                        displayName = resourceManager.getString(R.string.form_title),
                        attributes = trackerAttributes
                    )
                )
            }
        }
    }

    override suspend fun getPrograms(): List<Program> = withContext(Dispatchers.IO) {
        d2.programModule().programs().blockingGet()
    }

    override suspend fun getPrograms(programs: Map<String?, Int?>): List<Program> =
        withContext(Dispatchers.IO) {
            val uids = programs.keys.filterNotNull()

            d2.programModule().programs()
                .byUid().`in`(uids)
                .blockingGet()
                .sortedBy { program ->
                    programs[program.uid()] ?: Int.MAX_VALUE
                }
        }

    override suspend fun hasPermissionToWrite(program: String) = withContext(Dispatchers.IO) {
        d2.program(program)
            ?.access()
            ?.data()
            ?.write()
            ?: false
    }

    override suspend fun hasPermissionToWrite(
        program: String,
        programStage: String
    ) = withContext(Dispatchers.IO) {
        val hasAccessToProgram = hasPermissionToWrite(program)

        hasAccessToProgram && d2.programStage(programStage)
            ?.access()
            ?.data()
            ?.write()
            ?: false
    }
}
