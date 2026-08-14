package org.saudigitus.campaign.core.data.repository.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.dhis2.mobile.commons.providers.PreferenceProvider
import org.hisp.dhis.android.core.D2
import org.saudigitus.campaign.core.data.models.datastore.ApiDatastoreModel
import org.saudigitus.campaign.core.data.models.datastore.appconfig.toJson
import org.saudigitus.campaign.core.data.models.datastore.global.toJson
import org.saudigitus.campaign.core.data.models.entity.DatastoreEntity
import org.saudigitus.campaign.core.data.repository.ApiDatastore
import org.saudigitus.campaign.core.data.repository.SyncHelperRepository
import org.saudigitus.campaign.core.utils.Constants

class SyncHelperRepositoryImpl(
    private val d2: D2,
    private val apiDatastore: ApiDatastore,
    private val preferenceProvider: PreferenceProvider,
) : SyncHelperRepository {

    override suspend fun downloadDatastore() = withContext(Dispatchers.IO) {
        try {
            val baseUrl = d2.systemInfoModule().systemInfo().blockingGet()?.contextPath()
            val campaignId = preferenceProvider.getString(Constants.DATASTORE_NAMESPACE)
            val datastore = d2.httpServiceClient().get<ApiDatastoreModel> {
                this.absoluteUrl(
                    "${baseUrl}/api/routes/dhis2-campaign/run/api/campaigns/${campaignId}/metadata",
                    isExternalRequest = false
                )
            }

            preferenceProvider.setValue(Constants.DATASTORE_DOWNLOAD_ERROR, false)
            if (datastore.global?.toJson() == null || datastore.values?.toJson() == null) return@withContext

            apiDatastore.create(
                DatastoreEntity(
                    key = Constants.DS_GLOBAL_KEY,
                    value = datastore.global.toJson().orEmpty()
                ),
                DatastoreEntity(
                    key = Constants.DATASTORE_KEY,
                    value = datastore.values.toJson().orEmpty()
                ),
            )
        } catch (_: Exception) {
            preferenceProvider.setValue(Constants.DATASTORE_DOWNLOAD_ERROR, true)
        }
    }
}