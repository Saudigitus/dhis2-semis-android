package org.saudigitus.semis.core.data.utils

import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.saudigitus.semis.core.data.model.SearchTeiModel
import org.saudigitus.semis.core.data.model.person.PersonAttribute
import org.saudigitus.semis.core.data.model.person.PersonIdentity
import org.saudigitus.semis.core.data.model.person.PersonTag
import org.saudigitus.semis.core.data.model.person.identity
import org.saudigitus.semis.core.data.model.person.personIdentity
import javax.inject.Inject

class Transformations @Inject constructor(
    private val d2: D2,
    private val valueDisplay: ValueDisplay,
) {

    private lateinit var currentProgram: String
    private val orgUnitNameCache = HashMap<String?, String?>()
    private val displayOrgUnitCache = HashMap<String, Boolean>()
    private val attributeMetadataCache = HashMap<String, List<AttributeMetadata>>()

    fun transform(
        tei: TrackedEntityInstance?,
        program: String?,
        enrollment: Enrollment? = null,
    ): SearchTeiModel {
        val searchTei = SearchTeiModel()
        searchTei.tei = tei
        currentProgram = program ?: ""

        val values = tei?.trackedEntityAttributeValues()
        if (values != null) {
            val metadata = attributeMetadata(program, tei.trackedEntityType())
            val personAttributes = metadata.mapNotNull { attribute ->
                val stored = values.firstOrNull { it.trackedEntityAttribute() == attribute.uid }
                    ?: return@mapNotNull null
                val shown = valueDisplay.of(stored.value(), attribute.valueType, attribute.optionSet)
                    ?: return@mapNotNull null

                // The cards list what the program shows in the list, as they always have; the
                // identity reads every attribute, because the identifier and the photo may be
                // attributes the list does not show.
                if (attribute.displayInList) {
                    addAttribute(searchTei, stored, attribute.label, shown)
                }

                PersonAttribute(
                    uid = attribute.uid,
                    label = attribute.label,
                    value = shown,
                    valueType = attribute.valueType,
                    unique = attribute.unique,
                    displayInList = attribute.displayInList,
                    hasOptionSet = attribute.optionSet != null,
                    tag = attribute.colouredOptions?.get(stored.value()),
                )
            }
            searchTei.personIdentity = personIdentity(personAttributes)
        }

        if (enrollment != null) {
            searchTei.addEnrollment(enrollment)
            searchTei.setCurrentEnrollment(enrollment)
            searchTei.enrolledOrgUnit = orgUnitName(
                searchTei.selectedEnrollment.organisationUnit()
            )
        } else {
            searchTei.enrolledOrgUnit = orgUnitName(searchTei.tei.organisationUnit())
        }

        searchTei.displayOrgUnit = displayOrgUnit()
        return searchTei
    }

    /** Who the person [teiUid] is, read with the shared rule for [program]. */
    fun identityOf(teiUid: String, program: String): PersonIdentity? =
        d2.trackedEntityModule().trackedEntityInstances()
            .withTrackedEntityAttributeValues()
            .uid(teiUid)
            .blockingGet()
            ?.let { tei -> transform(tei, program).identity() }

    private fun addAttribute(
        searchTei: SearchTeiModel,
        attrValue: TrackedEntityAttributeValue,
        label: String,
        shown: String,
    ) {
        val attrValueBuilder = TrackedEntityAttributeValue.builder()
        attrValueBuilder.value(shown)
            .created(attrValue.created())
            .lastUpdated(attrValue.lastUpdated())
            .trackedEntityAttribute(attrValue.trackedEntityAttribute())
            .trackedEntityInstance(searchTei.tei.uid())
        searchTei.addAttributeValue(label, attrValueBuilder.build())
    }

    /**
     * The attributes of [program] in program order, or those of the tracked entity type when
     * there is no program, with what the identity rule needs to know about each.
     *
     * Read once per program: the metadata cannot change while a list is on screen, and a list
     * transforms every person in it.
     */
    private fun attributeMetadata(program: String?, trackedEntityType: String?): List<AttributeMetadata> {
        val key = program ?: "type:$trackedEntityType"
        return attributeMetadataCache.getOrPut(key) {
            val ordered = if (program != null) {
                d2.programModule().programTrackedEntityAttributes()
                    .byProgram().eq(program)
                    .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
                    .blockingGet()
                    .mapNotNull { programAttribute ->
                        programAttribute.trackedEntityAttribute()?.uid()
                            ?.let { it to (programAttribute.displayInList() == true) }
                    }
            } else {
                d2.trackedEntityModule().trackedEntityTypeAttributes()
                    .byTrackedEntityTypeUid().eq(trackedEntityType)
                    .blockingGet()
                    .mapNotNull { typeAttribute ->
                        typeAttribute.trackedEntityAttribute()?.uid()
                            ?.let { it to (typeAttribute.displayInList() == true) }
                    }
            }

            ordered.mapNotNull { (uid, displayInList) ->
                val attribute = d2.trackedEntityModule().trackedEntityAttributes()
                    .uid(uid)
                    .blockingGet()
                    ?: return@mapNotNull null
                val optionSet = attribute.optionSet()?.uid()

                AttributeMetadata(
                    uid = uid,
                    label = attribute.displayFormName()?.takeIf { it.isNotBlank() }
                        ?: attribute.displayName().orEmpty(),
                    valueType = attribute.valueType(),
                    unique = attribute.unique() == true,
                    displayInList = displayInList,
                    optionSet = optionSet,
                    colouredOptions = optionSet?.let(::colouredOptions),
                )
            }
        }
    }

    /**
     * The options of [optionSet] as tags, keyed by code, when every option has a colour.
     *
     * An option set where only some options are coloured is not meant to be read as tags, so it
     * gives none rather than colouring a value here and leaving the next one plain.
     */
    private fun colouredOptions(optionSet: String): Map<String, PersonTag>? {
        val options = d2.optionModule().options()
            .byOptionSetUid().eq(optionSet)
            .blockingGet()
        if (options.isEmpty()) return null

        val tags = options.mapNotNull { option ->
            val code = option.code() ?: return@mapNotNull null
            val color = option.style()?.color()?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            code to PersonTag(label = option.displayName() ?: code, color = color)
        }

        return tags.takeIf { it.size == options.size }?.toMap()
    }

    /**
     * Whether the cards need to name the school at all.
     *
     * They do only when the user captures this program in more than one place, which is a
     * question about the capture scope alone. The search scope holds other people's schools,
     * possibly the whole country, so counting it would answer the wrong question and read tens
     * of thousands of rows for every learner on the screen. Counted in the database and
     * remembered per program, because the answer cannot change while the screen is open.
     */
    private fun displayOrgUnit(): Boolean =
        displayOrgUnitCache.getOrPut(currentProgram) {
            d2.organisationUnitModule().organisationUnits()
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                .byProgramUids(listOf(currentProgram))
                .blockingCount() > 1
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
}

/** What the identity rule reads about one attribute. */
private data class AttributeMetadata(
    val uid: String,
    val label: String,
    val valueType: ValueType?,
    val unique: Boolean,
    val displayInList: Boolean,
    val optionSet: String?,
    val colouredOptions: Map<String, PersonTag>?,
)
