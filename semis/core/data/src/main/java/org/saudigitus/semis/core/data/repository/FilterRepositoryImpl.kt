package org.saudigitus.semis.core.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.hisp.dhis.android.core.D2
import javax.inject.Inject

class FilterRepositoryImpl
@Inject constructor(
    val d2: D2,
    val configRepository: AppConfigRepository
) : FilterRepository {
    override suspend fun getFilters(program: String) = withContext(Dispatchers.IO) {
        val appConfig = configRepository.getAppConfig(program)

        return@withContext appConfig?.filters
    }
}