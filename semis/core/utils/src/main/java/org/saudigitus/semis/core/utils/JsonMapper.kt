package org.saudigitus.semis.core.utils

import kotlinx.serialization.json.Json

object JsonMapper {
    val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }
}