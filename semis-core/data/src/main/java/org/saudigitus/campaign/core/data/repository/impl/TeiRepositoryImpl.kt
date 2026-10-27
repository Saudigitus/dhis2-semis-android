package org.saudigitus.campaign.core.data.repository.impl

import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.dhis2.commons.bindings.enrollment
import org.dhis2.commons.bindings.program
import org.dhis2.commons.bindings.trackedEntityType
import org.dhis2.commons.resources.ResourceManager
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceCreateProjection
import org.hisp.dhis.android.core.trackedentity.search.TrackedEntityInstanceQueryCollectionRepository
import org.saudigitus.campaign.core.data.R
import org.saudigitus.campaign.core.data.models.FilterModel
import org.saudigitus.campaign.core.data.models.FormFieldEntity
import org.saudigitus.campaign.core.data.models.OptionModel
import org.saudigitus.campaign.core.data.models.SearchTeiModel
import org.saudigitus.campaign.core.data.models.datastore.appconfig.DisplayMode
import org.saudigitus.campaign.core.data.models.datastore.appconfig.FilterProcessor
import org.saudigitus.campaign.core.data.models.datastore.appconfig.FilterType
import org.saudigitus.campaign.core.data.models.datastore.appconfig.RenderType
import org.saudigitus.campaign.core.data.repository.D2MetadataHistoryRepository
import org.saudigitus.campaign.core.data.repository.DatastoreRepository
import org.saudigitus.campaign.core.data.repository.OptionRepository
import org.saudigitus.campaign.core.data.repository.ProgramRepository
import org.saudigitus.campaign.core.data.repository.TeiRepository
import org.saudigitus.campaign.core.data.utils.Transformations
import org.saudigitus.campaign.core.data.utils.eventsWithTrackedDataValues
import org.saudigitus.campaign.core.utils.location.EvaluateDistance
import org.saudigitus.campaign.core.utils.location.state.CoordinateState
import java.util.Date
import javax.inject.Inject

class TeiRepositoryImpl
@Inject constructor(
    val d2: D2,
    val transformations: Transformations,
    private val d2MetadataHistoryRepository: D2MetadataHistoryRepository,
    private val optionRepository: OptionRepository,
    private val programRepository: ProgramRepository,
    private val datastoreRepository: DatastoreRepository,
    private val resourceManager: ResourceManager
) : TeiRepository {

    override suspend fun getTeiByUid(
        teiUid: String,
        program: String
    ): SearchTeiModel? = withContext(Dispatchers.IO) {
        val tei = d2.trackedEntityModule()
            .trackedEntityInstances()
            .byUid().eq(teiUid)
            .withTrackedEntityAttributeValues()
            .one()
            .blockingGet() ?: return@withContext null

        val enrollment = program.let {
            d2.enrollmentModule()
                .enrollments()
                .byTrackedEntityInstance().eq(teiUid)
                .byProgram().eq(it)
                .one()
                .blockingGet()
        }

        transformations.transform(tei, program, enrollment)
    }

    override suspend fun create(
        orgUnit: String,
        program: String,
        fields: List<FormFieldEntity>
    ): String = withContext(Dispatchers.IO) {
        val teiType = d2.program(program)?.trackedEntityType()
            ?: throw IllegalStateException(
                resourceManager.getString(R.string.tei_type_not_found, program)
            )

        val uid = d2.trackedEntityModule().trackedEntityInstances().blockingAdd(
            TrackedEntityInstanceCreateProjection.builder()
                .organisationUnit(orgUnit)
                .trackedEntityType(teiType.uid().orEmpty())
                .build()
        )

        for (field in fields) {
            d2.trackedEntityModule()
                .trackedEntityAttributeValues()
                .value(field.uid, uid)
                .blockingSet(field.value)
        }

        val displayMode = getDisplayMode(program)
        if (displayMode?.createdByUser == true) {
            d2MetadataHistoryRepository.createTEIHistory(program, uid)
        }

        return@withContext uid
    }

    override suspend fun update(
        tei: String,
        fields: List<FormFieldEntity>
    ): String = withContext(Dispatchers.IO) {
        for (field in fields) {
            d2.trackedEntityModule()
                .trackedEntityAttributeValues()
                .value(field.uid, tei)
                .blockingSet(field.value)
        }

        return@withContext tei
    }

    override suspend fun getTrackerEntities(
        ou: String,
        program: String,
        stage: String,
        dataElementIds: List<String>,
        dataValues: List<String>
    ) = flow {
        val events = d2.eventsWithTrackedDataValues(ou, program, stage)

        val teis = events
            .asSequence()
            .filter { event ->
                val dataElements = event.trackedEntityDataValues()
                    ?.associate { it.dataElement() to it.value() }
                    ?: emptyMap()

                dataElements.keys.containsAll(dataElementIds) &&
                    dataElements.values.containsAll(dataValues)
            }
            .mapNotNull { event ->
                val enrollment =
                    d2.enrollment(event.enrollment().orEmpty()) ?: return@mapNotNull null
                val tei = d2.trackedEntityModule()
                    .trackedEntityInstances()
                    .byUid()
                    .eq(enrollment.trackedEntityInstance())
                    .withTrackedEntityAttributeValues()
                    .one()
                    .blockingGet()

                transformations.transform(tei, program, enrollment)
            }
            .toList()

        emit(teis)
    }
        .flowOn(Dispatchers.IO)
        .catch { _ ->
            emit(emptyList())
        }

    override fun getTrackerEntitiesByProgram(program: String) =
        getTEIRepository(program)
            .getPagingData(2)
            .map { pagingData ->
                withContext(Dispatchers.IO) {
                    pagingData.map {
                        val enrollment = getEnrollment(program, it.uid())
                        transformations.transform(it, program, enrollment)
                    }
                }
            }


    @OptIn(ExperimentalCoroutinesApi::class)
    override fun searchTrackerEntities(
        program: String,
        query: String,
        filters: List<FilterModel>,
        coordinates: Pair<FilterModel, CoordinateState>?
    ): Flow<PagingData<SearchTeiModel>> = flow {
        val displayMode = getDisplayMode(program)
        val scopedTeiUids = getScopedTeiUids(program, displayMode)

        if (scopedTeiUids?.isEmpty() == true) {
            emit(emptySet())
            return@flow
        }

        var trackerUids = getTEIRepository(program, applyCreatedByUserScope = false)
            .blockingGetUids()
            .toSet()

        if (scopedTeiUids != null) {
            trackerUids = trackerUids.intersect(scopedTeiUids.toSet())
        }

        if (query.isNotBlank()) {
            trackerUids = trackerUids.intersect(
                getSearchTrackerUids(program, query, trackerUids)
            )
        }

        filters
            .filter { it.selectedOption != null }
            .forEach { filter ->
                trackerUids = trackerUids.intersect(
                    getFilterTrackerUids(
                        program = program,
                        filter = filter,
                        value = filter.selectedOption?.code.orEmpty(),
                        candidateTeiUids = trackerUids,
                    )
                )
            }

        coordinates?.let { coordinateFilter ->
            trackerUids = trackerUids.intersect(
                getCoordinateTrackerUids(
                    filter = coordinateFilter.first,
                    coordinateState = coordinateFilter.second,
                    candidateTeiUids = trackerUids,
                )
            )
        }

        emit(trackerUids)
    }
        .flowOn(Dispatchers.IO)
        .flatMapLatest { trackerUids ->
            if (trackerUids.isEmpty()) {
                flowOf(PagingData.empty())
            } else {
                getTEIRepository(program, applyCreatedByUserScope = false)
                    .byTrackedEntities().`in`(trackerUids.toList())
                    .getPagingData(1)
            }
        }
        .map { pagingData ->
            withContext(Dispatchers.IO) {
                pagingData.map { tei ->
                    val enrollment = getEnrollment(program, tei.uid())
                    transformations.transform(tei, program, enrollment)
                }
            }
        }

    private suspend fun getSearchTrackerUids(
        program: String,
        query: String,
        candidateTeiUids: Set<String>,
    ): Set<String> {
        val filters = datastoreRepository.getFilters(program)
        val attributeUids = filters
            .filter { it.type != FilterType.DATA_ELEMENT && it.renderType == RenderType.SEARCH_FIELD }
            .mapNotNull { it.uid }

        val attributeTrackerUids = if (attributeUids.isEmpty()) {
            emptySet()
        } else {
            d2.trackedEntityModule()
                .trackedEntityAttributeValues()
                .byTrackedEntityInstance().`in`(candidateTeiUids.toList())
                .byTrackedEntityAttribute().`in`(attributeUids)
                .byValue().like(query)
                .blockingGet()
                .mapNotNull { it.trackedEntityInstance() }
                .toSet()
        }

        val dataElementTrackerUids = filters
            .filter { it.type == FilterType.DATA_ELEMENT && it.renderType == RenderType.SEARCH_FIELD }
            .mapNotNull { filter ->
                filter.uid?.let { uid ->
                    getDataElementTrackerUids(
                        program = program,
                        dataElementUid = uid,
                        programStage = filter.programStage,
                        processor = filter.resolvedFilterProcessor,
                        value = query,
                        candidateTeiUids = candidateTeiUids,
                        exactMatch = false,
                    )
                }
            }
            .fold(emptySet<String>()) { accumulator, uids -> accumulator + uids }

        return attributeTrackerUids + dataElementTrackerUids
    }

    private fun getFilterTrackerUids(
        program: String,
        filter: FilterModel,
        value: String,
        candidateTeiUids: Set<String>,
    ): Set<String> = when (filter.type) {
        FilterType.ATTRIBUTE -> d2.trackedEntityModule()
            .trackedEntityAttributeValues()
            .byTrackedEntityInstance().`in`(candidateTeiUids.toList())
            .byTrackedEntityAttribute().eq(filter.uid)
            .byValue().eq(value)
            .blockingGet()
            .mapNotNull { it.trackedEntityInstance() }
            .toSet()

        FilterType.DATA_ELEMENT -> getDataElementTrackerUids(
            program = program,
            dataElementUid = filter.uid,
            programStage = filter.programStage,
            processor = filter.filterProcessor,
            value = value,
            candidateTeiUids = candidateTeiUids,
            exactMatch = true,
        )

        FilterType.UNKNOWN -> emptySet()
    }

    private fun getDataElementTrackerUids(
        program: String,
        dataElementUid: String,
        programStage: String?,
        processor: FilterProcessor?,
        value: String,
        candidateTeiUids: Set<String>,
        exactMatch: Boolean,
    ): Set<String> {
        if (candidateTeiUids.isEmpty()) return emptySet()

        var eventRepository = d2.eventModule().events()
            .byProgramUid().eq(program)
            .byDeleted().isFalse
            .byTrackedEntityInstanceUids(candidateTeiUids.toList())

        if (!programStage.isNullOrBlank()) {
            eventRepository = eventRepository.byProgramStageUid().eq(programStage)
        }

        val matchingEvents = if (exactMatch) {
            eventRepository.byDataValue(dataElementUid).eq(value).blockingGet()
        } else {
            eventRepository.byDataValue(dataElementUid).like(value).blockingGet()
        }

        if (processor != FilterProcessor.LAST_EVENT) {
            return getTrackerUidsForEvents(matchingEvents)
        }

        val matchingEventUids = matchingEvents.mapTo(mutableSetOf()) { it.uid() }
        val latestEvents = eventRepository
            .blockingGet()
            .groupBy { it.enrollment() }
            .values
            .mapNotNull { events -> events.maxWithOrNull(eventRecencyComparator) }
            .filter { it.uid() in matchingEventUids }

        return getTrackerUidsForEvents(latestEvents)
    }

    private fun getTrackerUidsForEvents(events: List<Event>): Set<String> {
        val enrollmentUids = events.mapNotNull { it.enrollment() }.distinct()
        if (enrollmentUids.isEmpty()) return emptySet()

        return d2.enrollmentModule()
            .enrollments()
            .byUid().`in`(enrollmentUids)
            .blockingGet()
            .mapNotNull { it.trackedEntityInstance() }
            .toSet()
    }

    private fun getCoordinateTrackerUids(
        filter: FilterModel,
        coordinateState: CoordinateState,
        candidateTeiUids: Set<String>,
    ): Set<String> = d2.trackedEntityModule()
        .trackedEntityAttributeValues()
        .byTrackedEntityInstance().`in`(candidateTeiUids.toList())
        .byTrackedEntityAttribute().eq(filter.uid)
        .blockingGet()
        .mapNotNull { attribute ->
            val coordinates = attribute.value()
                ?.trim('[', ']')
                ?.split(",")
                ?.map(String::trim)
                ?: return@mapNotNull null

            val longitude = coordinates.getOrNull(0)?.toDoubleOrNull() ?: return@mapNotNull null
            val latitude = coordinates.getOrNull(1)?.toDoubleOrNull() ?: return@mapNotNull null
            val distance = EvaluateDistance.distanceInMeters(
                coordinateState.latitude ?: return@mapNotNull null,
                coordinateState.longitude ?: return@mapNotNull null,
                latitude,
                longitude,
            )

            attribute.trackedEntityInstance()
                ?.takeIf { distance <= (filter.range?.toDouble() ?: 15.0) }
        }
        .toSet()

    override suspend fun getFilters(program: String) = withContext(Dispatchers.IO) {
        val filters = datastoreRepository.getFilters(program)

        val filterUids = filters
            .filter { it.type != FilterType.DATA_ELEMENT && it.renderType == RenderType.SEARCH_FIELD }
            .mapNotNull { it.uid }

        val attributeFilters = programRepository.getTrackedEntityAttribute(program, true)
            .filterNot { it.uid in filterUids }
            .map { attr ->
                val filter = filters.find {
                    it.uid == attr.uid && it.type != FilterType.DATA_ELEMENT
                }

                if (filter != null) {
                    FilterModel(
                        uid = filter.uid.orEmpty(),
                        displayName = filter.displayName?.ifEmpty { attr.displayFormName.orEmpty() }
                            ?: attr.displayFormName.orEmpty(),
                        optionModel = getOptionModels(attr.optionSetUid),
                        type = filter.type ?: FilterType.UNKNOWN,
                        renderType = filter.renderType ?: RenderType.UNKNOWN,
                        programStage = filter.programStage,
                        filterProcessor = filter.resolvedFilterProcessor,
                        range = filter.range,
                    )
                } else {
                    FilterModel(
                        uid = attr.uid,
                        displayName = attr.displayFormName.orEmpty(),
                        optionModel = getOptionModels(attr.optionSetUid),
                        type = FilterType.ATTRIBUTE,
                        renderType = RenderType.CHIP,
                    )
                }
            }

        val dataElementFilters = filters
            .filter { it.type == FilterType.DATA_ELEMENT }
            .mapNotNull { filter ->
                val uid = filter.uid ?: return@mapNotNull null
                val dataElement = d2.dataElementModule().dataElements().uid(uid).blockingGet()
                    ?: return@mapNotNull null

                FilterModel(
                    uid = uid,
                    displayName = filter.displayName?.ifEmpty {
                        dataElement.displayFormName().orEmpty()
                    } ?: dataElement.displayFormName().orEmpty(),
                    optionModel = getOptionModels(dl = uid),
                    type = FilterType.DATA_ELEMENT,
                    renderType = filter.renderType ?: RenderType.UNKNOWN,
                    programStage = filter.programStage,
                    filterProcessor = filter.resolvedFilterProcessor,
                    range = filter.range,
                )
            }

        attributeFilters + dataElementFilters
    }

    private companion object {
        val eventRecencyComparator = compareBy<Event> { it.eventDate()?.time ?: Long.MIN_VALUE }
            .thenBy { it.lastUpdated()?.time ?: Long.MIN_VALUE }
            .thenBy { it.uid() }
    }

    override suspend fun getTrackedEntityType(uid: String) = withContext(Dispatchers.IO) {
        d2.trackedEntityType(uid)?.displayName().orEmpty()
    }

    override suspend fun getTrackedEntityAttributeValues(tei: String) = withContext(Dispatchers.IO) {
        d2.trackedEntityModule().trackedEntityAttributeValues()
            .byTrackedEntityInstance().eq(tei)
            .blockingGet()
    }

    private fun getEnrollment(program: String, tei: String): Enrollment? {
        return d2.enrollmentModule().enrollments()
            .byProgram().eq(program)
            .byTrackedEntityInstance().eq(tei)
            .byStatus().eq(EnrollmentStatus.ACTIVE)
            .one().blockingGet()
    }

    private suspend fun getOptionModels(
        optionSetUid: String? = null,
        dl: String? = null,
    ): List<OptionModel> {
        return if (optionSetUid == null && dl != null) {
            optionRepository.getOptionsByDataElement(dl)
                .map {
                    OptionModel(
                        uid = it.uid(),
                        code = it.code(),
                        displayName = it.displayName(),
                        sortOrder = it.sortOrder(),
                    )
                }.sortedBy { it.sortOrder }
        } else {
            optionRepository.getOptions(optionSetUid.orEmpty()).map {
                OptionModel(
                    uid = it.uid(),
                    code = it.code(),
                    displayName = it.displayName(),
                    sortOrder = it.sortOrder(),
                )
            }.sortedBy { it.sortOrder }
        }
    }

    private suspend fun getDisplayMode(program: String): DisplayMode? {
        return datastoreRepository.getAppConfig(program)?.default?.displayMode
    }

    private fun getScopedTeiUids(
        program: String,
        displayMode: DisplayMode?
    ): List<String>? {
        if (displayMode?.createdByUser != true) return null

        return runBlocking {
            d2MetadataHistoryRepository.getTEIHistoryUids(program)
        }
    }

    private fun getTEIRepository(
        program: String,
        applyCreatedByUserScope: Boolean = true
    ): TrackedEntityInstanceQueryCollectionRepository {
        val displayMode = runBlocking { getDisplayMode(program) }
        val defaultOrder = displayMode?.sort?.split(":")
        val attrId = defaultOrder?.firstOrNull().orEmpty()
        val orderByDirection = if (defaultOrder?.lastOrNull()
                .orEmpty().lowercase() == "desc"
        ) RepositoryScope.OrderByDirection.DESC
        else RepositoryScope.OrderByDirection.ASC

        var query = d2.trackedEntityModule()
            .trackedEntityInstanceQuery()
            .byProgram()
            .eq(program)
            .let {
                if (!defaultOrder.isNullOrEmpty()) {
                    it.orderByAttribute(attrId).eq(orderByDirection)
                } else {
                    it
                }
            }

        if (applyCreatedByUserScope) {
            val teiList = getScopedTeiUids(program, displayMode)

            query = if (teiList?.isEmpty() == true) {
                query.byTrackedEntities().`in`("")
            } else if (teiList != null) {
                query.byTrackedEntities().`in`(teiList)
            } else {
                query
            }
        }

        query = when (displayMode?.listView) {
            "TODAY" -> query.byLastUpdatedDate().afterOrEqual(Date())
            else -> query
        }

        return query.orderByLastUpdated()
            .eq(RepositoryScope.OrderByDirection.DESC)
    }
}
