package org.saudigitus.semis.core.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.dhis2.commons.bindings.dataElement
import org.hisp.dhis.android.core.D2
import org.saudigitus.semis.core.data.model.OptionModel
import org.saudigitus.semis.core.data.rules.RuleEngineRepository
import org.saudigitus.semis.core.data.utils.optionByOptionSet
import org.saudigitus.semis.core.data.utils.optionsNotInOptionGroup
import org.saudigitus.semis.core.data.utils.optionsNotInOptionsSets
import javax.inject.Inject

class FilterRepositoryImpl
@Inject constructor(
    val d2: D2,
    val configRepository: AppConfigRepository,
    val ruleEngineRepository: RuleEngineRepository
) : FilterRepository {
    override suspend fun getFilters(program: String) = withContext(Dispatchers.IO) {
        val appConfig = configRepository.getAppConfig(program)

        return@withContext appConfig?.filters
    }

    override suspend fun getOptions(
        orgUnit: String?,
        program: String,
        dataElement: String
    ) = withContext(Dispatchers.IO) {
        val optionSet = d2.dataElement(dataElement)?.optionSetUid()

        val hideOptions = ruleEngineRepository.applyOptionRules(orgUnit, program, dataElement)

        val rawOptions = when {
            hideOptions.isEmpty() -> d2.optionByOptionSet(optionSet)
            orgUnit != null -> d2.optionsNotInOptionGroup(hideOptions, optionSet)
            else -> d2.optionsNotInOptionsSets(hideOptions, optionSet)
        }

        return@withContext rawOptions
            .map {
                OptionModel(
                    uid = it.uid(),
                    displayName = it.displayName().orEmpty(),
                    code = it.code().orEmpty(),
                    sortOrder = it.sortOrder()
                )
            }
            .sortedBy { it.sortOrder }
    }
}