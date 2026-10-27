package org.saudigitus.campaign.core.utils

import kotlinx.serialization.json.Json

object JsonMapper {
    val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        prettyPrint = true
        explicitNulls = false
    }

    val minifiedJson = Json {
        prettyPrint = false
        encodeDefaults = false
        explicitNulls = false
        ignoreUnknownKeys = true
    }
}