package org.saudigitus.semis.core.data.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.dhis2.commons.resources.ResourceManager
import org.saudigitus.semis.core.data.model.Module
import org.saudigitus.semis.core.data.utils.isEnabled
import org.saudigitus.semis.core.utils.Constants.ABSENTEEISM
import org.saudigitus.semis.core.utils.Constants.APP_MODULES
import org.saudigitus.semis.core.utils.Constants.ATTENDANCE
import org.saudigitus.semis.core.utils.Constants.PERFORMANCE
import org.saudigitus.semis.core.utils.JsonMapper
import javax.inject.Inject

class AppModulesRepositoryImpl
@Inject constructor(
    val configRepository: AppConfigRepository,
    val resourceManager: ResourceManager
) : AppModulesRepository {
    override suspend fun getModules(program: String) = withContext(Dispatchers.IO) {
        val appConfig = configRepository.getAppConfig(program)

        val json = readJsonFromAssets(resourceManager.context)
        val parsedModules = JsonMapper.json.decodeFromString<List<Module>>(json)

        val modules = parsedModules.map {
            when (it.key) {
                ATTENDANCE -> it.isEnabled(appConfig?.attendance?.enabled ?: it.enabled)
                ABSENTEEISM -> it.isEnabled(appConfig?.absenteeism?.enabled ?: it.enabled)
                PERFORMANCE -> it.isEnabled(appConfig?.performance?.enabled ?: it.enabled)
                else -> it
            }
        }.filter { it.enabled }

        return@withContext modules
    }

    private fun readJsonFromAssets(context: Context): String {
        return context.assets.open(APP_MODULES).bufferedReader().use { it.readText() }
    }
}