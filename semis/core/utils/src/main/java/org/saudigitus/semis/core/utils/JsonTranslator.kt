package org.saudigitus.semis.core.utils

object JsonTranslator {
    inline fun <reified T> translateFromJson(json: String?): T? =
        json?.let { JsonMapper.json.decodeFromString(it) }
}