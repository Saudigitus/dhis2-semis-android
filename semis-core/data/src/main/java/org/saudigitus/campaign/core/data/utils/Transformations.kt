package org.saudigitus.campaign.core.data.utils

import org.dhis2.bindings.userFriendlyValue
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.saudigitus.campaign.core.data.models.DataValueItem
import org.saudigitus.campaign.core.data.models.EventWithDataValues
import org.saudigitus.campaign.core.data.models.SearchTeiModel
import javax.inject.Inject

class Transformations @Inject constructor(private val d2: D2) {

    private lateinit var currentProgram: String
    private val orgUnitNameCache = HashMap<String?, String?>()


    fun transform(
        tei: TrackedEntityInstance?,
        program: String?,
        enrollment: Enrollment? = null,
    ): SearchTeiModel {
        val searchTei = SearchTeiModel()
        searchTei.tei = tei
        currentProgram = program ?: ""

        if (tei?.trackedEntityAttributeValues() != null) {
            if (program != null) {
                val programAttributes = d2.programModule().programTrackedEntityAttributes()
                    .byProgram().eq(program)
                    .byDisplayInList().isTrue
                    .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
                    .blockingGet()

                for (programAttribute in programAttributes) {
                    val attribute = d2.trackedEntityModule().trackedEntityAttributes()
                        .uid(programAttribute.trackedEntityAttribute()!!.uid())
                        .blockingGet()

                    for (attrValue in tei.trackedEntityAttributeValues()!!) {
                        if (attrValue.trackedEntityAttribute() == attribute?.uid()) {
                            addAttribute(searchTei, attrValue, attribute)
                            break
                        }
                    }
                }
            } else {
                val typeAttributes = d2.trackedEntityModule().trackedEntityTypeAttributes()
                    .byTrackedEntityTypeUid().eq(searchTei.tei.trackedEntityType())
                    .byDisplayInList().isTrue
                    .blockingGet()
                for (typeAttribute in typeAttributes) {
                    val attribute = d2.trackedEntityModule().trackedEntityAttributes()
                        .uid(typeAttribute.trackedEntityAttribute()!!.uid())
                        .blockingGet()
                    for (attrValue in tei.trackedEntityAttributeValues()!!) {
                        if (attrValue.trackedEntityAttribute() == attribute?.uid()) {
                            addAttribute(searchTei, attrValue, attribute)
                            break
                        }
                    }
                }
            }
        }

        if (enrollment != null) {
            searchTei.addEnrollment(enrollment)
            searchTei.setCurrentEnrollment(enrollment)
            searchTei.enrolledOrgUnit = orgUnitName(
                searchTei.selectedEnrollment.organisationUnit()
            )
            searchTei.events = d2.eventModule().events()
                .byEnrollmentUid().eq(enrollment.uid())
                .byDeleted().isFalse
                .withTrackedEntityDataValues()
                .blockingGet()
                .map { transformEvent(it) }
        } else {
            searchTei.enrolledOrgUnit = orgUnitName(searchTei.tei.organisationUnit())
        }

        searchTei.displayOrgUnit = displayOrgUnit()
        return searchTei
    }

    private fun addAttribute(
        searchTei: SearchTeiModel,
        attrValue: TrackedEntityAttributeValue,
        attribute: TrackedEntityAttribute?,
    ) {
        val friendlyValue = attrValue.userFriendlyValue(d2)

        val attrValueBuilder = TrackedEntityAttributeValue.builder()
        attrValueBuilder.value(friendlyValue)
            .created(attrValue.created())
            .lastUpdated(attrValue.lastUpdated())
            .trackedEntityAttribute(attrValue.trackedEntityAttribute())
            .trackedEntityInstance(searchTei.tei.uid())
        searchTei.addAttributeValue(attribute?.displayFormName(), attrValueBuilder.build())
    }

    private fun displayOrgUnit(): Boolean {
        return d2.organisationUnitModule().organisationUnits()
            .byProgramUids(listOf(currentProgram))
            .blockingGet().size > 1
    }

    private fun orgUnitName(orgUnitUid: String?): String? {
        if (!orgUnitNameCache.containsKey(orgUnitUid)) {
            val organisationUnit = d2.organisationUnitModule()
                .organisationUnits()
                .uid(orgUnitUid)
                .blockingGet()
            orgUnitNameCache[orgUnitUid] = organisationUnit!!.displayName()
        }
        return orgUnitNameCache[orgUnitUid]
    }

    private fun transformEvent(event: Event): EventWithDataValues {
        val orgUnitName = event.organisationUnit()?.let { ouUid ->
            orgUnitName(ouUid)
        }

        val dataValues = event.trackedEntityDataValues()?.map { dv ->
            val dataElement = d2.dataElementModule()
                .dataElements()
                .uid(dv.dataElement())
                .blockingGet()

            DataValueItem(
                dataElementUid = dv.dataElement() ?: "",
                label = dataElement?.displayFormName()
                    ?: dataElement?.displayName()
                    ?: dv.dataElement()
                    ?: "",
                value = dv.value() ?: "",
            )
        } ?: emptyList()

        val programStageName = event.programStage()?.let { stageUid ->
            d2.programModule().programStages()
                .uid(stageUid)
                .blockingGet()?.displayName()
        }

        return EventWithDataValues(
            eventUid = event.uid(),
            eventDate = event.eventDate()?.toString()?.substringBefore("T"),
            status = event.status()?.name,
            orgUnitName = orgUnitName,
            programStageUid = event.programStage(),
            programStageName = programStageName,
            dataValues = dataValues,
        )
    }
}