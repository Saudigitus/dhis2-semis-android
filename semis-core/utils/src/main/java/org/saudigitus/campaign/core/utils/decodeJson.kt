package org.saudigitus.campaign.core.utils

fun decodeJson(clsString: String?): String {
    val jsonPart = clsString
        ?.removePrefix("JsonWrapper(")
        ?.removeSuffix(")")

    val finalStr = jsonPart?.replace("=", ":").orEmpty()
        .removePrefix("json:")

    return finalStr.trim()
}