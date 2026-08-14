package org.saudigitus.campaign.core.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.saudigitus.campaign.core.utils.JsonMapper

@Serializable
data class QrResult(
    val uid: String? = null,
    @SerialName("name")
    val displayName: String? = null,
) {
    companion object {
        fun fromJson(json: String?): QrResult? {
            if (json.isNullOrBlank()) return null

            return try {
                JsonMapper.json.decodeFromString<QrResult>(json)
            } catch (_: Exception) {
                null
            }
        }
    }
}
