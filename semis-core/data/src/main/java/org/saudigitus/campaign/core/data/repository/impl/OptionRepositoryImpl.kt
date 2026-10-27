package org.saudigitus.campaign.core.data.repository.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.dhis2.commons.bindings.dataElement
import org.hisp.dhis.android.core.D2
import org.saudigitus.campaign.core.data.repository.OptionRepository
import org.saudigitus.campaign.core.data.utils.optionByOptionSet
import org.saudigitus.campaign.core.data.utils.optionsByOptionSetAndCode
import javax.inject.Inject

class OptionRepositoryImpl
@Inject constructor(
    val d2: D2
) : OptionRepository {
    override suspend fun getOptions(optionSetUid: String) = withContext(Dispatchers.IO) {
        d2.optionByOptionSet(optionSetUid)
    }

    override suspend fun getOptionsByDataElement(
        dataElement: String
    ) = withContext(Dispatchers.IO) {
        val optionSet = d2.dataElement(dataElement)?.optionSetUid()

         d2.optionByOptionSet(optionSet)
    }

    override suspend fun getOptionsByCode(
        dataElement: String,
        codes: List<String>
    ) = withContext(Dispatchers.IO) {
        val optionSet = d2.dataElement(dataElement)?.optionSetUid()

        d2.optionsByOptionSetAndCode(optionSet, codes)
    }
}