package org.saudigitus.semis.core.data.utils

import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.option.Option
import org.saudigitus.semis.core.data.model.Module
import org.saudigitus.semis.core.data.model.OptionModel
import org.saudigitus.semis.core.utils.DateHelper
import java.sql.Date

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

fun D2.eventsWithTrackedDataValuesByDate(
    program: String,
    stage: String,
    date: String? = DateHelper.formatDate(System.currentTimeMillis())
): Event? = eventModule().events()
    .byProgramUid().eq(program)
    .byProgramStageUid().eq(stage)
    .byEventDate().eq(Date.valueOf(date))
    .byDeleted().isFalse
    .withTrackedEntityDataValues()
    .one()
    .blockingGet()

fun D2.optionByOptionSet(
    optionSet: String?,
): List<Option> = optionModule()
    .options()
    .byOptionSetUid().eq(optionSet)
    .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
    .blockingGet()

/**
 * Uids of every option that belongs to any of [optionGroups].
 *
 * Option level program rules address whole groups while the list they restrict is made of single
 * options, so the membership has to be resolved before a rule can be applied to an option list.
 */
fun D2.optionUidsInOptionGroups(
    optionGroups: List<String>,
): Set<String> {
    if (optionGroups.isEmpty()) return emptySet()

    return optionModule()
        .optionGroups()
        .byUid().`in`(optionGroups)
        .withOptions()
        .blockingGet()
        .flatMap { optionGroup ->
            optionGroup.options()?.map { it.uid() } ?: emptyList()
        }
        .toSet()
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


fun Module.isEnabled(enabled: Boolean) = copy(enabled = enabled)

fun Option.toOptionModel() = OptionModel(
    uid = this.uid(),
    code = this.code(),
    displayName = this.displayName(),
    sortOrder = this.sortOrder(),
)