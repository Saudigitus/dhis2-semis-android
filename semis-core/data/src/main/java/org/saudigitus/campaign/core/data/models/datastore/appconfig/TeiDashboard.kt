package org.saudigitus.campaign.core.data.models.datastore.appconfig

import kotlinx.serialization.Serializable

@Serializable
data class TeiDashboard(
    val eventRegistration: EventRegistration?
)
