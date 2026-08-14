package org.saudigitus.campaign.core.data.models.datastore.appconfig

import org.saudigitus.campaign.core.utils.JsonMapper

object AppConfig {
    fun fromJson(json: String?): List<AppConfigItem>? {
        if (json.isNullOrBlank()) return null

        return try {
            JsonMapper.json.decodeFromString<List<AppConfigItem>>(json)
        } catch (_: Exception) {
            null
        }
    }
}

fun AppConfigItem.toJson(): String? {
    return try {
        JsonMapper.minifiedJson.encodeToString(this).trim()
    } catch (_: Exception) {
        null
    }
}

fun List<AppConfigItem>.toJson(): String? {
    return try {
        JsonMapper.minifiedJson.encodeToString(this).trim()
    } catch (_: Exception) {
        null
    }
}