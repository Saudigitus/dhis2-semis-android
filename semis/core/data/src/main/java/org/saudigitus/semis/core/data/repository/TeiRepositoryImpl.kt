package org.saudigitus.semis.core.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.dhis2.commons.bindings.enrollment
import org.hisp.dhis.android.core.D2
import org.saudigitus.semis.core.data.utils.Transformations
import org.saudigitus.semis.core.data.utils.eventsWithTrackedDataValues
import javax.inject.Inject

class TeiRepositoryImpl
@Inject constructor(
    val d2: D2,
    val transformations: Transformations
) : TeiRepository {

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
                val enrollment = d2.enrollment(event.enrollment().orEmpty()) ?: return@mapNotNull null
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
    .catch { e ->
        emit(emptyList())
    }
}