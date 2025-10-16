package org.saudigitus.semis.core.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.saudigitus.semis.core.data.model.OptionModel
import javax.inject.Inject

class FilterRepositoryImpl
@Inject constructor(
    val configRepository: AppConfigRepository,
    val optionRepository: OptionRepository
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
        val rawOptions = optionRepository.getOptions(orgUnit, program, dataElement)

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