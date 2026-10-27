package org.saudigitus.semis.core.data.model.app_config

import kotlinx.serialization.builtins.ListSerializer
import org.saudigitus.semis.core.utils.JsonMapper

class SEMISConfig(private val items: List<SEMISConfigItem>) {
    fun toJson(): String =
        JsonMapper.json
            .encodeToString(
                ListSerializer(SEMISConfigItem.serializer()),
                items
            )

    companion object {
        fun fromJson(json: String?): List<SEMISConfigItem>? =
            json?.let {
                JsonMapper.json.decodeFromString(
                    ListSerializer(SEMISConfigItem.serializer()),
                    it
                )
            }
    }
}

