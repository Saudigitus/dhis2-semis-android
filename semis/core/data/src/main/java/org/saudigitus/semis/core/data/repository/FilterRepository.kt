package org.saudigitus.semis.core.data.repository

import org.saudigitus.semis.core.data.model.OptionModel
import org.saudigitus.semis.core.data.model.app_config.Filters

interface FilterRepository {
    suspend fun getFilters(program: String): Filters?
    suspend fun getOptions(orgUnit: String?, program: String, dataElement: String): List<OptionModel>
}

