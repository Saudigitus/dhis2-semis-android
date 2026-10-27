package org.saudigitus.semis.core.utils

fun decodeJson(clsString: String?): String {
    val jsonPart = clsString
        ?.removePrefix("JsonWrapper(")
        ?.removeSuffix(")")

    val finalStr = jsonPart?.replace("=", ":").orEmpty()
        .removePrefix("json:")

    return finalStr
}