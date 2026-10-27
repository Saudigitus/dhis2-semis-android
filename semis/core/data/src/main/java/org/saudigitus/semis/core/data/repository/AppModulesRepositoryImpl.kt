package org.saudigitus.semis.core.data.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.dhis2.commons.resources.ResourceManager
import org.saudigitus.semis.core.data.model.Module
import org.saudigitus.semis.core.data.utils.isEnabled
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
     * and eventually transferred.
     *
     * That order comes from the bundled asset and is therefore the same on every server, which is
     * at odds with the rest of SEMIS, where the datastore decides. It should be read from the
     * datastore too, with the asset kept only as the default, so that a deployment can present the
     * modules in the order its own users work in. That change is not made here.
     *
     * A module the configuration disables, or does not configure at all, is dropped from the list
     * rather than shown as unavailable, so what this returns is exactly what the home screen draws.
     * A program without a performance setting, as staff usually is, therefore offers no
     * performance, instead of a module that only says its configuration is missing.
     *
     * Only the modules this app has a screen for are ever offered. A module the configuration
     * enables but the app does not have, such as absenteeism, is never listed, because opening it
     * would have nowhere to go.
     */
    override suspend fun getModules(program: String) = withContext(Dispatchers.IO) {
        val appConfig = configRepository.getAppConfig(program)

        val json = readJsonFromAssets(resourceManager.context)
        val parsedModules = JsonMapper.json.decodeFromString<List<Module>>(json)

        val modules = parsedModules.map {
            when (it.key) {
                ATTENDANCE -> it.isEnabled(appConfig?.attendance?.enabled == true)
                ENROLLMENT -> it.isEnabled(appConfig?.registration?.enabled == true)
                PERFORMANCE -> it.isEnabled(appConfig?.performance?.enabled == true)
                TRANSFER -> it.isEnabled(appConfig?.transfer?.enabled == true)
                else -> it.isEnabled(false)
            }
        }.filter { it.enabled }

        return@withContext modules
    }

    private fun readJsonFromAssets(context: Context): String {
        return context.assets.open(APP_MODULES).bufferedReader().use { it.readText() }
    }
}
