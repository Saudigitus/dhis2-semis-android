package org.saudigitus.semis.core.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.dhis2.commons.bindings.dataElement
import org.hisp.dhis.android.core.D2
import org.saudigitus.semis.core.data.rules.RuleEngineRepository
import org.saudigitus.semis.core.data.utils.optionByOptionSet
import org.saudigitus.semis.core.data.utils.optionsByOptionSetAndCode
import org.saudigitus.semis.core.data.utils.optionsNotInOptionGroup
import org.saudigitus.semis.core.data.utils.optionsNotInOptionsSets
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

        val hideOptions = ruleEngineRepository.applyOptionRules(ou, program, dataElement)

        when {
            hideOptions.isEmpty() -> d2.optionByOptionSet(optionSet)
            ou != null -> d2.optionsNotInOptionGroup(hideOptions, optionSet)
            else -> d2.optionsNotInOptionsSets(hideOptions, optionSet)
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