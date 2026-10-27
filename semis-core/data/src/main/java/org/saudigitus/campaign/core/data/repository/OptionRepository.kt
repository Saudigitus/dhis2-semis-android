package org.saudigitus.campaign.core.data.repository

import org.hisp.dhis.android.core.option.Option

interface OptionRepository {

    suspend fun getOptions(
        optionSetUid: String,
    ): List<Option>

    suspend fun getOptionsByDataElement(
        dataElement: String,
    ): List<Option>

    suspend fun getOptionsByCode(
        dataElement: String,
        codes: List<String>,
    ): List<Option>
}