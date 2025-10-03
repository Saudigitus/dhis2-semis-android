package org.saudigitus.semis.core.data.repository

import org.saudigitus.semis.core.data.model.app_config.Filters

interface FilterRepository {
    suspend fun getFilters(program: String): Filters?
}

