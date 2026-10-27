package org.saudigitus.campaign.core.data.models.datastore.global

import org.saudigitus.campaign.core.utils.JsonMapper

object GlobalConfig {
    fun fromJson(json: String?): GlobalConfigItem? {
        if (json.isNullOrBlank()) return null

        return try {
            JsonMapper.json.decodeFromString<GlobalConfigItem>(json)
        } catch (_: Exception) {
            null
        }
    }
}

fun GlobalConfigItem.toJson(): String? {
    return try {
        JsonMapper.minifiedJson.encodeToString(this).trim()
    } catch (_: Exception) {
        null
    }
}

fun List<GlobalConfigItem>.toJson(): String? {
    return try {
        JsonMapper.minifiedJson.encodeToString(this).trim()
    } catch (_: Exception) {
        null
    }
}