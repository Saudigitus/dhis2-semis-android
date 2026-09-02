package org.saudigitus.campaign.core.data.models.datastore.appconfig

import kotlinx.serialization.Serializable

@Serializable
data class DisplayMode(
    val listView: String? = null,
    val createdByUser: Boolean? = false,
    val sort: String? = null,
)
