package org.saudigitus.campaign.core.data.models.datastore.appconfig

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EventRegistration(
    @SerialName("numberOfCycles")
    val dataElement: String? = null,
    val registration: String? = null,
    @SerialName("registrationEventsLimit")
    val registrationEventsLimit: List<RegistrationEventLimit>? = null
)

@Serializable
data class RegistrationEventLimit(
    val registration: String? = null,
    val limit: Int? = null
)
