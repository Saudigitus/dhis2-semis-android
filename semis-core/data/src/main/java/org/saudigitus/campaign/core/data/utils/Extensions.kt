package org.saudigitus.campaign.core.data.utils

import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.option.Option
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.saudigitus.campaign.core.data.models.OptionModel
import org.saudigitus.campaign.core.data.models.datastore.appconfig.FormValidation
import org.saudigitus.campaign.core.utils.JsonMapper

fun D2.eventsWithTrackedDataValues(
    ou: String,
    program: String,
    stage: String,
): List<Event> = eventModule().events()
    .byOrganisationUnitUid().eq(ou)
    .byProgramUid().eq(program)
    .byProgramStageUid().eq(stage)
    .byDeleted().isFalse
    .withTrackedEntityDataValues()
    .blockingGet()

fun D2.optionByOptionSet(
    optionSet: String?,
): List<Option> = optionModule()
    .options()
    .byOptionSetUid().eq(optionSet)
    .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
    .blockingGet()

fun D2.optionsNotInOptionsSets(
    options: List<String>,
    optionSet: String?,
): List<Option> = optionModule()
    .options()
    .byUid().notIn(options)
    .byOptionSetUid().eq(optionSet)
    .orderByDisplayName(RepositoryScope.OrderByDirection.ASC)
    .blockingGet()

fun D2.optionsNotInOptionGroup(
    optionGroups: List<String>,
    optionSet: String?,
): List<Option> = optionModule()
    .optionGroups()
    .byUid().notIn(optionGroups)
    .byOptionSetUid().eq(optionSet)
    .withOptions()
    .orderByDisplayName(RepositoryScope.OrderByDirection.ASC)
    .blockingGet()
    .flatMap {
        it.options() ?: emptyList()
    }.flatMap {
        optionModule()
            .options()
            .byUid().eq(it.uid())
            .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
            .blockingGet()
    }

fun D2.optionsByOptionSetAndCode(
    optionSet: String?,
    codes: List<String>,
): List<Option> = optionModule()
    .options()
    .byCode().`in`(codes)
    .byOptionSetUid().eq(optionSet)
    .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
    .blockingGet()


fun Option.toOptionModel() = OptionModel(
    uid = this.uid(),
    code = this.code(),
    displayName = this.displayName(),
    sortOrder = this.sortOrder(),
)

fun List<org.saudigitus.campaign.core.data.models.datastore.appconfig.Option>.toOptionsModel() = this.map {
    OptionModel(
        uid = it.uid.orEmpty(),
        code = it.code,
        displayName = it.name,
        sortOrder = null,
    )
}

fun <T> T.createdByUser(
    enabled: Boolean,
    uids: List<String>?,
    filter: T.(List<String>) -> T
): T {
    return when {
        !enabled -> this
        uids.isNullOrEmpty() -> filter(listOf(""))
        else -> filter(uids)
    }
}

fun List<TrackedEntityAttributeValue>.byAttribute(uid: String) = this.find {
    it.trackedEntityAttribute() == uid
}

fun List<FormValidation>.toJson(): String? {
    return try {
        if (this.isEmpty()) return null
        JsonMapper.minifiedJson.encodeToString(this).trim()
    } catch (_: Exception) {
        null
    }
}