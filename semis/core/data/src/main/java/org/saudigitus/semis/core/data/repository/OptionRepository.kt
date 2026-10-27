package org.saudigitus.semis.core.data.repository

import org.hisp.dhis.android.core.option.Option

interface OptionRepository {
    suspend fun getOptions(
        ou: String? = null,
        program: String,
        dataElement: String,
    ): List<Option>

    suspend fun getOptionsByCode(
        dataElement: String,
        codes: List<String>,
    ): List<Option>
}