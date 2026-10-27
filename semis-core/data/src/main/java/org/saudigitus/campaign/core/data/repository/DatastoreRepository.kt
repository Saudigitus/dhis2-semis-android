package org.saudigitus.campaign.core.data.repository

import org.saudigitus.campaign.core.data.models.datastore.appconfig.AppConfigItem
import org.saudigitus.campaign.core.data.models.datastore.appconfig.Filter
import org.saudigitus.campaign.core.data.models.datastore.appconfig.FilterType
import org.saudigitus.campaign.core.data.models.datastore.appconfig.FormValidation
import org.saudigitus.campaign.core.data.models.datastore.appconfig.FormValidationType
import org.saudigitus.campaign.core.data.models.datastore.global.GlobalConfigItem

interface DatastoreRepository {
    suspend fun getAppConfig(program: String): AppConfigItem?
    suspend fun getCustomNavigation(program: String): String?
    suspend fun getGlobalConfig(key: String): GlobalConfigItem?

    suspend fun getFormValidations(
        program: String,
        type: FormValidationType? = null
    ): List<FormValidation>

    suspend fun getFilters(program: String, type: FilterType? = null): List<Filter>

    suspend fun hasConfigs(): Boolean

    suspend fun cleanTable()
}