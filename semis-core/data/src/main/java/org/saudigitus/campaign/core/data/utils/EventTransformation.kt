package org.saudigitus.campaign.core.data.utils

import android.content.Context
import jakarta.inject.Inject
import org.dhis2.bindings.userFriendlyValue
import org.dhis2.commons.data.EventModel
import org.dhis2.commons.data.EventViewModelType
import org.dhis2.commons.resources.DhisPeriodUtils
import org.dhis2.commons.resources.MetadataIconProvider
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.category.CategoryCombo
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.period.PeriodType
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.saudigitus.campaign.core.data.R
import java.util.Locale

class EventTransformation @Inject constructor(
    private val context: Context,
    private val d2: D2,
    private val metadataIconProvider: MetadataIconProvider,
) {

    private val periodUtils: Lazy<DhisPeriodUtils> = lazy {
        DhisPeriodUtils(
            d2,
            context.getString(R.string.period_span_default_label),
            context.getString(R.string.week_period_span_default_label),
            context.getString(R.string.biweek_period_span_default_label),
        )
    }

    fun transformation(event: Event, programStage: ProgramStage): EventModel {
        return EventModel(
            EventViewModelType.EVENT,
            programStage,
            event,
            0,
            null,
            isSelected = true,
            canAddNewEvent = true,
            orgUnitName = orgUnitName(event.organisationUnit()),
            orgUnitIsInCaptureScope = hasAccessToEvent(event.organisationUnit(), event.status()),
            catComboName = getCatOptionComboName(event.attributeOptionCombo()),
            dataElementValues =
                getEventValues(
                    event.uid(),
                    event.programStage(),
                ),
            groupedByStage = true,
            displayDate = if (event.eventDate() != null || event.dueDate() != null) {
                periodUtils.value.getPeriodUIString(
                    programStage.periodType() ?: PeriodType.Daily,
                    event.eventDate() ?: event.dueDate()!!,
                    Locale.getDefault(),
                )
            } else "---",
            nameCategoryOptionCombo =
                getCategoryComboFromOptionCombo(event.attributeOptionCombo())?.displayName(),
            metadataIconData =
                metadataIconProvider(
                    programStage.style(),
                    SurfaceColor.Primary,
                ),
        )
    }

    private fun getCatOptionComboName(categoryOptionComboUid: String?): String? =
        categoryOptionComboUid?.let {
            d2
                .categoryModule()
                .categoryOptionCombos()
                .uid(categoryOptionComboUid)
                .blockingGet()
                ?.displayName()
        }

    private fun getCategoryComboFromOptionCombo(categoryOptionComboUid: String?): CategoryCombo? {
        val catOptionComboUid =
            categoryOptionComboUid?.let {
                d2
                    .categoryModule()
                    .categoryOptionCombos()
                    .uid(it)
                    .blockingGet()
                    ?.categoryCombo()
                    ?.uid()
            }

        return catOptionComboUid?.let {
            d2
                .categoryModule()
                .categoryCombos()
                .uid(it)
                .blockingGet()
        }
    }

    private fun orgUnitName(orgUnitUid: String?): String =
        d2
            .organisationUnitModule()
            .organisationUnits()
            .uid(orgUnitUid)
            .blockingGet()
            ?.displayName().orEmpty()

    private fun hasAccessToEvent(
        eventOrgUnitUid: String?,
        eventStatus: EventStatus?,
    ): Boolean =
        if (eventStatus == EventStatus.SCHEDULE ||
            eventStatus == EventStatus.OVERDUE
        ) {
            eventOrgUnitUid?.let {
                d2
                    .organisationUnitModule()
                    .organisationUnitService()
                    .isInCaptureScope(it)
                    .blockingGet()
            } ?: true
        } else {
            true
        }

    private fun getEventValues(
        eventUid: String,
        stageUid: String?,
    ): List<Pair<String, String?>> {
        val displayInListDataElements =
            d2
                .programModule()
                .programStageDataElements()
                .byProgramStage()
                .eq(stageUid)
                .byDisplayInReports()
                .isTrue
                .blockingGet()
                .map {
                    it.dataElement()?.uid()!!
                }
        return if (displayInListDataElements.isNotEmpty()) {
            displayInListDataElements.mapNotNull {
                val valueRepo =
                    d2
                        .trackedEntityModule()
                        .trackedEntityDataValues()
                        .value(eventUid, it)
                val de =
                    d2
                        .dataElementModule()
                        .dataElements()
                        .uid(it)
                        .blockingGet()
                if (isAcceptedValueType(de?.valueType())) {
                    Pair(
                        de?.displayFormName() ?: de?.displayName() ?: "",
                        if (valueRepo.blockingExists()) {
                            valueRepo.blockingGet().userFriendlyValue(d2)
                        } else {
                            "-"
                        },
                    )
                } else {
                    null
                }
            }
        } else {
            emptyList()
        }
    }

    private fun isAcceptedValueType(valueType: ValueType?): Boolean =
        when (valueType) {
            ValueType.IMAGE, ValueType.COORDINATE, ValueType.FILE_RESOURCE -> false
            else -> true
        }
}