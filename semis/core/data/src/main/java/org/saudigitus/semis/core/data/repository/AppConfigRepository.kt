package org.saudigitus.semis.core.data.repository

import org.saudigitus.semis.core.data.model.app_config.SEMISConfigItem

interface AppConfigRepository {
    suspend fun getAppConfig(program: String): SEMISConfigItem?
}

