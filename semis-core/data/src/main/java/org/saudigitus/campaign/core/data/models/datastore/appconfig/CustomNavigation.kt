package org.saudigitus.campaign.core.data.models.datastore.appconfig

import kotlinx.serialization.Serializable

@Serializable
data class CustomNavigation(
    val program: String?,
    val navigateTo: String?
)
