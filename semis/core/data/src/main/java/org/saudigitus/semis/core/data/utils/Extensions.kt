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


fun Module.isEnabled(enabled: Boolean) = copy(enabled = enabled)

fun Option.toOptionModel() = OptionModel(
    uid = this.uid(),
    code = this.code(),
    displayName = this.displayName(),
    sortOrder = this.sortOrder(),
)