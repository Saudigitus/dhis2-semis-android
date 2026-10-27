package org.saudigitus.campaign.core.data.repository.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.dhis2.mobile.commons.providers.PreferenceProvider
import org.hisp.dhis.android.core.D2
import org.saudigitus.campaign.core.data.models.datastore.appconfig.AppConfig
import org.saudigitus.campaign.core.data.models.datastore.appconfig.AppConfigItem
import org.saudigitus.campaign.core.data.models.datastore.appconfig.FilterType
import org.saudigitus.campaign.core.data.models.datastore.appconfig.FormValidation
import org.saudigitus.campaign.core.data.models.datastore.appconfig.FormValidationType
import org.saudigitus.campaign.core.data.models.datastore.global.GlobalConfig
import org.saudigitus.campaign.core.data.repository.ApiDatastore
import org.saudigitus.campaign.core.data.repository.DatastoreRepository
import org.saudigitus.campaign.core.utils.Constants
import org.saudigitus.campaign.core.utils.Constants.DATASTORE_KEY
import org.saudigitus.campaign.core.utils.Constants.DATASTORE_NAMESPACE
import org.saudigitus.campaign.core.utils.decodeJson

class DatastoreRepositoryImpl(
    private val d2: D2,
    private val apiDatastore: ApiDatastore,
    private val preferences: PreferenceProvider,
) : DatastoreRepository {
    override suspend fun getAppConfig(program: String) = withContext(Dispatchers.IO) {
        val config = getAppConfigs()

        return@withContext config?.firstOrNull { it.program == program }
    }

    override suspend fun getCustomNavigation(program: String) = withContext(Dispatchers.IO) {
        val config = getAppConfigs()

        val customNavigation = config
            ?.asSequence()
            ?.mapNotNull { it.default?.customNavigation }
            ?.flatten()
            ?.firstOrNull { it.program == program }
            ?.navigateTo

        return@withContext customNavigation
            ?: config?.firstOrNull { it.program == program }?.navigateTo
    }

    override suspend fun getGlobalConfig(key: String) = withContext(Dispatchers.IO) {
        val decodedJson = apiDatastore.getDatastoreByKey(key)
        val globalConfig = GlobalConfig.fromJson(decodedJson?.value)

        return@withContext globalConfig
    }

    override suspend fun getFormValidations(
        program: String,
        type: FormValidationType?
    ): List<FormValidation> = withContext(Dispatchers.IO) {
        when (type) {
            FormValidationType.ENROLLMENT -> {
                getAppConfig(program)?.formValidations?.filter {
                    it.type == FormValidationType.ENROLLMENT.name
                } ?: emptyList()
            }
            FormValidationType.EVENT -> {
                getAppConfig(program)?.formValidations?.filter {
                    it.type == FormValidationType.EVENT.name
                } ?: emptyList()
            }
            else -> getAppConfig(program)?.formValidations ?: emptyList()
        }
    }

    override suspend fun getFilters(program: String, type: FilterType?) =
        withContext(Dispatchers.IO) {
            val config = getAppConfig(program)

            return@withContext if (type != null) {
                config?.filters
                    ?.filter { it.type == type }
                    ?: emptyList()
            } else {
                config?.filters ?: emptyList()
            }
        }

    override suspend fun hasConfigs() = withContext(Dispatchers.IO) {
        val config = getAppConfigs()

        val hasGlobalConfig = getGlobalConfig(Constants.DS_GLOBAL_KEY)

        !config.isNullOrEmpty() && hasGlobalConfig != null
    }

    override suspend fun cleanTable() = withContext(Dispatchers.IO) {
        apiDatastore.deleteAll()
    }

    private suspend fun getAppConfigs(): List<AppConfigItem>? {
        val decodedJson = apiDatastore.getDatastoreByKey(DATASTORE_KEY)
        return AppConfig.fromJson(decodedJson?.value)
    }

    private fun decodedDataStore(key: String): String {
        val nameSpace = preferences.getString(DATASTORE_NAMESPACE)

        val dataStore = d2.dataStoreModule()
            .dataStore()
            .byNamespace().eq(nameSpace)
            .byKey().eq(key)
            .one().blockingGet()

        return decodeJson(dataStore?.value())
    }
}
