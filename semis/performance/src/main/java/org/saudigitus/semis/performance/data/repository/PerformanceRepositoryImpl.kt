package org.saudigitus.semis.performance.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.dhis2.commons.bindings.enrollment
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.event.Event
import org.saudigitus.semis.core.data.model.SearchTeiModel
import org.saudigitus.semis.core.data.repository.EventRepository
import org.saudigitus.semis.core.data.repository.OptionRepository
import org.saudigitus.semis.core.data.utils.toOptionModel
import org.saudigitus.semis.core.form.data.model.FormFieldData
import javax.inject.Inject

class PerformanceRepositoryImpl @Inject constructor(
    private val d2: D2,
    private val eventRepository: EventRepository,
    private val optionRepository: OptionRepository
) : PerformanceRepository {
    override suspend fun savePerformance(
        orgUnit: String,
        program: String,
        programStage: String,
        teiList: List<SearchTeiModel>,
        formFieldsData: List<FormFieldData>
    ) = withContext(Dispatchers.IO) {
        val data2Save = formFieldsData.filter { it.isUpdated }

        data2Save.forEach {
            val tei = teiList.find { student -> student.tei.uid() == it.tei }

            tei?.let { student ->
                eventRepository.saveEvent(
                    event = it.event,
                    orgUnit = orgUnit,
                    program = program,
                    programStage = programStage,
                    tei = student,
                    data = mapOf("dataElement" to Pair(it.dataElement, it.value.orEmpty())),
                    eventDate = null
                )
            }
        }
    }

    override suspend fun getEvents(
        ou: String,
        program: String,
        programStage: String,
        dataElement: String,
        teis: List<String>
    ) = withContext(Dispatchers.IO) {
        val events = eventRepository.getEvents(
            ou = ou,
            program = program,
            programStage = programStage,
            dataElement = dataElement,
            teis = teis
        ).map { event ->
            eventsTransform(event, program, dataElement)
        }

        val newEvents = createEventField(teis, events, dataElement)

        val mutableEvents = events.toMutableList()
        mutableEvents.addAll(newEvents ?: emptyList())

        mutableEvents.toList()
    }

    private fun createEventField(
        teis: List<String>,
        formFieldData: List<FormFieldData>,
        dataElement: String
    ): List<FormFieldData>? {
        val fieldTeis = formFieldData.map { it.tei }.distinct()
        val newTeis = teis.filterNot { it in fieldTeis }

        return if (newTeis.isNotEmpty()) {
            newTeis.map {
                FormFieldData(
                    tei = it,
                    dataElement = dataElement,
                )
            }
        } else null
    }

    private suspend fun eventsTransform(
        event: Event,
        program: String,
        dataElement: String,
    ): FormFieldData {
        val dataValue = event.trackedEntityDataValues()?.find { it.dataElement() == dataElement }
        val tei = d2.enrollment(event.enrollment().orEmpty())?.trackedEntityInstance().orEmpty()

        val option = optionRepository.getOptions(program = program, dataElement = dataElement)
            .find { it.code() == dataValue?.value() }
            ?.toOptionModel()

        return if (dataValue != null) {
            FormFieldData(
                tei = tei,
                event = event.uid(),
                dataElement = dataElement,
                value = dataValue.value(),
                optionModel = option
            )
        } else {
            FormFieldData(
                tei = tei,
                event = event.uid(),
                dataElement = dataElement,
                value = null,
                optionModel = null
            )
        }
    }
}