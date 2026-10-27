package org.saudigitus.campaign.core.data.models.datastore

import kotlinx.serialization.Serializable
import org.saudigitus.campaign.core.data.models.datastore.appconfig.AppConfigItem
import org.saudigitus.campaign.core.data.models.datastore.global.GlobalConfigItem

@Serializable
data class ApiDatastoreModel(
    val global: GlobalConfigItem? = null,
    val values: List<AppConfigItem>? = null,
)
