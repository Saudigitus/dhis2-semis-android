package org.saudigitus.campaign.core.data.models

import kotlinx.serialization.Serializable

@Serializable
data class LoggingPayload(
    val type: String,
    val error: String,
    val data: List<String>,
    val user: String,
    val time: String,
    val server: String,
)