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
import org.saudigitus.semis.core.utils.Constants.ENROLLMENT
import org.saudigitus.semis.core.utils.Constants.PERFORMANCE
import org.saudigitus.semis.core.utils.Constants.TRANSFER
import org.saudigitus.semis.core.utils.JsonMapper
import javax.inject.Inject

class AppModulesRepositoryImpl
@Inject constructor(
    val configRepository: AppConfigRepository,
    val resourceManager: ResourceManager
) : AppModulesRepository {
    /**
     * Reads the modules the home screen offers, in the order they are shown.
     *
     * The order follows the life of a learner: enrolled first, marked present day to day, graded,
     * and eventually transferred. Absenteeism sits beside attendance because it belongs to the same
     * daily routine.
     *
     * That order comes from the bundled asset and is therefore the same on every server, which is
     * at odds with the rest of SEMIS, where the datastore decides. It should be read from the
     * datastore too, with the asset kept only as the default, so that a deployment can present the
     * modules in the order its own users work in. That change is not made here.
     *
     * A module the configuration disables is dropped from the list rather than shown as
     * unavailable, so what this returns is exactly what the home screen draws.
     */
    override suspend fun getModules(program: String) = withContext(Dispatchers.IO) {
        val appConfig = configRepository.getAppConfig(program)

        val json = readJsonFromAssets(resourceManager.context)
        val parsedModules = JsonMapper.json.decodeFromString<List<Module>>(json)

        val modules = parsedModules.map {
            when (it.key) {
                ATTENDANCE -> it.isEnabled(appConfig?.attendance?.enabled ?: it.enabled)
                ENROLLMENT -> it.isEnabled(appConfig?.registration?.enabled ?: it.enabled)
                ABSENTEEISM -> it.isEnabled(appConfig?.absenteeism?.enabled ?: it.enabled)
                PERFORMANCE -> it.isEnabled(appConfig?.performance?.enabled ?: it.enabled)
                TRANSFER -> it.isEnabled(appConfig?.transfer?.enabled ?: it.enabled)
                else -> it
            }
        }.filter { it.enabled }

        return@withContext modules
    }

    private fun readJsonFromAssets(context: Context): String {
        return context.assets.open(APP_MODULES).bufferedReader().use { it.readText() }
    }
}
