package org.saudigitus.campaign.core.utils.location.state

data class CoordinateState(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val accuracy: Float? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
