package org.saudigitus.semis.core.utils

import kotlinx.serialization.json.Json

object JsonMapper {
    val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        prettyPrint = true
        explicitNulls = false
    }
}