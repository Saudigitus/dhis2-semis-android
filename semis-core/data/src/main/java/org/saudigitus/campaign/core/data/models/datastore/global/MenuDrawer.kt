package org.saudigitus.campaign.core.data.models.datastore.global

import kotlinx.serialization.Serializable

@Serializable
data class MenuDrawer(
    val showUserQrCode: Boolean = false,
    val showAppVersion: Boolean = true,
)