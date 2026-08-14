package org.saudigitus.campaign.core.data.repository.impl

import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.dhis2.commons.bindings.event
import org.dhis2.commons.bindings.program
import org.dhis2.commons.bindings.programStage
import org.dhis2.commons.data.EventModel
import org.dhis2.commons.date.DateUtils
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventCreateProjection
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.event.search.EventQueryCollectionRepository
import org.hisp.dhis.android.core.program.ProgramType
import org.saudigitus.campaign.core.data.models.FormSectionEntity
import org.saudigitus.campaign.core.data.models.datastore.appconfig.DisplayMode
import org.saudigitus.campaign.core.data.repository.D2MetadataHistoryRepository
import org.saudigitus.campaign.core.data.repository.DatastoreRepository
import org.saudigitus.campaign.core.data.repository.EventRepository
import org.saudigitus.campaign.core.data.utils.EventTransformation
import org.saudigitus.campaign.core.utils.Constants
import org.saudigitus.campaign.core.utils.DateHelper
import timber.log.Timber
import java.sql.Date
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor(
    val d2: D2,
    private val eventTransformation: EventTransformation,
    private val d2MetadataHistoryRepository: D2MetadataHistoryRepository,
    private val datastoreRepository: DatastoreRepository,
) : EventRepository {

    private fun getAttributeOptionCombo() =
        d2.categoryModule().categoryOptionCombos()
            .byDisplayName().eq(Constants.DEFAULT).one().blockingGet()?.uid()

    private fun createEventProjection(
        enrollment: String? = null,
        ou: String,
        program: String,
        programStage: String,
    ): String {
        val projection = EventCreateProjection.builder()
            .organisationUnit(ou)
            .program(program).programStage(programStage)
            .attributeOptionCombo(getAttributeOptionCombo())

        return d2.eventModule().events()
            .blockingAdd(
                if (!enrollment.isNullOrEmpty()) {
                    projection.enrollment(enrollment).build()
                } else {
                    projection.build()
                }
            )
    }

    private fun eventUid(
        tei: String?,
        program: String,
        programStage: String,
        date: String?,
    ): String? {
        return if (!tei.isNullOrEmpty()) {
            d2.eventModule().events()
                .byTrackedEntityInstanceUids(listOf(tei))
                .byProgramUid().eq(program)
                .byProgramStageUid().eq(programStage)
                .byDeleted().isFalse
                .byEventDate().eq(Date.valueOf(date))
                .one().blockingGet()?.uid()
        } else {
            null
        }
    }

    override suspend fun saveEvent(
        orgUnit: String,
        program: String,
        tei: String?,
        enrollment: String?,
        eventDate: String?,
        formSections: List<FormSectionEntity>
    ): Boolean =
        withContext(Dispatchers.IO) {
            val date = eventDate ?: DateHelper.formatDate(System.currentTimeMillis())!!

            return@withContext try {
                val formFields = formSections.flatMap { it.formFields }
                val event = formSections.firstOrNull()?.eventUid
                val programStage = formSections.firstOrNull()?.programStage

                val uid = event ?: eventUid(
                    tei,
                    program,
                    programStage.orEmpty(),
                    date
                ) ?: createEventProjection(
                    enrollment,
                    orgUnit,
                    program,
                    programStage.orEmpty(),
                )

                for (field in formFields) {
                    d2.trackedEntityModule().trackedEntityDataValues()
                        .value(uid, field.uid)
                        .blockingSet(field.value)
                }

                val repository = d2.eventModule().events().uid(uid)
                repository.setEventDate(Date.valueOf(date))
                repository.setStatus(EventStatus.COMPLETED)

                val displayMode = getDisplayMode(program)
                val isWithoutRegistration =
                    d2.program(program)?.programType() == ProgramType.WITHOUT_REGISTRATION
                if (displayMode?.createdByUser == true && isWithoutRegistration) {
                    d2MetadataHistoryRepository.createEventHistory(program, uid)
                }

                uid.isNotEmpty()
            } catch (e: Exception) {
                Timber.tag("SAVE_EVENT").e(e)
                false
            }
        }

    override suspend fun getOrGenerateEvent(
        orgUnit: String,
        program: String,
        programStage: String,
        tei: String?,
        enrollment: String?
    ): Event? = withContext(Dispatchers.IO) {
        val isRepeatable = d2.programStage(programStage)?.repeatable()

        val uid = eventUid(
            tei,
            program,
            programStage,
            DateHelper.formatDate(System.currentTimeMillis())
        )

        val eventUid = if (uid != null && isRepeatable != true) {
            uid
        } else {
            createEventProjection(
                enrollment,
                orgUnit,
                program,
                programStage
            )
        }

        val repository = d2.eventModule().events().uid(eventUid)
        repository.setEventDate(Date.valueOf(DateHelper.formatDate(System.currentTimeMillis())))
        repository.setStatus(EventStatus.ACTIVE)

        d2.event(eventUid)
    }

    override suspend fun getEvents(
        teiUids: List<String>,
        program: String,
        programStage: String,
        eventDate: String?
    ) = withContext(Dispatchers.IO) {
        val date = if (eventDate != null) {
            Date.valueOf(eventDate)
        } else {
            DateUtils.getInstance().today
        }

        d2.eventModule().events()
            .byTrackedEntityInstanceUids(teiUids)
            .byProgramUid().eq(program)
            .byProgramStageUid().eq(programStage)
            .byDeleted().isFalse
            .byEventDate().eq(date)
            .withTrackedEntityDataValues()
            .blockingGet()
    }

    override suspend fun getEvents(
        ou: String,
        program: String,
        programStage: String,
        dataElement: String,
        teis: List<String>
    ) = withContext(Dispatchers.IO) {
        d2.eventModule().events()
            .byTrackedEntityInstanceUids(teis)
            .byOrganisationUnitUid().eq(ou)
            .byProgramUid().eq(program)
            .byProgramStageUid().eq(programStage)
            .withTrackedEntityDataValues()
            .blockingGet()
    }

    override fun getEvents(
        program: String,
        programStage: String?
    ): Flow<PagingData<EventModel>> {
        val ps = if (programStage == null) {
            d2.programModule().programStages()
                .byProgramUid().eq(program)
                .withAttributes()
                .one().blockingGet()
        } else d2.programStage(programStage)

        if (ps == null) return emptyFlow()

        return getEventRepository(program, ps.uid().orEmpty())
            .byIncludeDeleted().eq(false)
            .getPagingData(3)
            .map { pagingData ->
                withContext(Dispatchers.IO) {
                    pagingData.map {
                        eventTransformation.transformation(it, ps)
                    }
                }
            }
    }

    override suspend fun deleteEvent(eventUid: String) = withContext(Dispatchers.IO) {
        d2.eventModule().events().uid(eventUid).blockingDeleteIfExist()
    }

    private suspend fun getDisplayMode(program: String): DisplayMode? {
        return datastoreRepository.getAppConfig(program)?.default?.displayMode
    }

    private fun getEventRepository(
        program: String,
        programStage: String?
    ): EventQueryCollectionRepository {
        val displayMode = runBlocking { getDisplayMode(program) }
        val defaultOrder = displayMode?.sort?.split(":")
        val dataElementId = defaultOrder?.firstOrNull().orEmpty()
        val orderByDirection = if (defaultOrder?.lastOrNull()
                .orEmpty().lowercase() == "desc"
        ) RepositoryScope.OrderByDirection.DESC
        else RepositoryScope.OrderByDirection.ASC

        var repository = if (defaultOrder.isNullOrEmpty()) {
            d2.eventModule().eventQuery()
                .byProgram().eq(program)
        } else {
            d2.eventModule().eventQuery()
                .byProgram().eq(program)
                .orderByDataElement(dataElementId).eq(orderByDirection)
        }

        if (displayMode?.createdByUser == true) {
            val eventList = runBlocking {
                d2MetadataHistoryRepository.getEventHistoryUids(program)
            }

            repository = if (eventList.isEmpty()) {
                repository.byUid().`in`("")
            } else {
                repository.byUid().`in`(eventList)
            }
        }

        repository = when (displayMode?.listView) {
            "TODAY" -> if (displayMode.createdByUser == true) {
                repository
            } else {
                repository.byLastUpdated().afterOrEqual(Date(System.currentTimeMillis()))
            }
            else -> repository
        }

        return repository
            .byProgramStage().eq(programStage)
            .orderByLastUpdated().eq(RepositoryScope.OrderByDirection.DESC)
    }
}
