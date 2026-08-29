package org.saudigitus.semis.core.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.dhis2.commons.bindings.dataElement
import org.hisp.dhis.android.core.D2
import org.saudigitus.semis.core.data.rules.RuleEngineRepository
import org.saudigitus.semis.core.data.rules.isOptionVisible
import org.saudigitus.semis.core.data.utils.optionByOptionSet
import org.saudigitus.semis.core.data.utils.optionUidsInOptionGroups
import org.saudigitus.semis.core.data.utils.optionsByOptionSetAndCode
import javax.inject.Inject

class OptionRepositoryImpl
@Inject constructor(
    val d2: D2,
    val ruleEngineRepository: RuleEngineRepository
) : OptionRepository {
    override suspend fun getOptions(
        ou: String?,
        program: String,
        dataElement: String
    ) = withContext(Dispatchers.IO) {
        val optionSet = d2.dataElement(dataElement)?.optionSetUid()
        val options = d2.optionByOptionSet(optionSet)

        val effects = ruleEngineRepository.applyOptionRules(ou, program, dataElement)
        if (effects.restrictsNothing) {
            return@withContext options
        }

        val optionUidsToHide = effects.optionsToHide.toSet() +
            d2.optionUidsInOptionGroups(effects.optionGroupsToHide)
        val optionUidsToShow = effects.optionGroupsToShow
            .takeIf { it.isNotEmpty() }
            ?.let { d2.optionUidsInOptionGroups(it) }

        options.filter { option ->
            isOptionVisible(option.uid(), optionUidsToHide, optionUidsToShow)
        }
    }

    override suspend fun getOptionsByCode(
        dataElement: String,
        codes: List<String>
    ) = withContext(Dispatchers.IO) {
        val optionSet = d2.dataElement(dataElement)?.optionSetUid()

        d2.optionsByOptionSetAndCode(optionSet, codes)
    }
}