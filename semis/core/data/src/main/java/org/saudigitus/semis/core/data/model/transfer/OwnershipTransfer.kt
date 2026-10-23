package org.saudigitus.semis.core.data.model.transfer

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * The name the server gives the organisation unit of an ownership transfer.
 *
 * DHIS2 2.41 only knows `ou`, 2.43 only `orgUnit`, and 2.42 takes either but refuses both
 * at once. The SDK always sends `ou`, which 2.43 refuses, so the app sends the transfer
 * itself and names the parameter after the server it talks to.
 */
fun ownershipOrgUnitParameter(serverIsAtLeast242: Boolean): String =
    if (serverIsAtLeast242) "orgUnit" else "ou"

/**
 * The school that owns the learner for [program], read from a tracked entity of the
 * tracker API, or null when the answer does not say.
 */
fun programOwnerOf(trackedEntityJson: String, program: String): String? =
    runCatching {
        Json.parseToJsonElement(trackedEntityJson).jsonObject["programOwners"]
            ?.jsonArray
            ?.map { it.jsonObject }
            ?.firstOrNull { it["program"]?.jsonPrimitive?.contentOrNull == program }
            ?.get("orgUnit")
            ?.jsonPrimitive
            ?.contentOrNull
    }.getOrNull()

private val SERVER_MESSAGE = Regex("\"message\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"")

/** The message of a DHIS2 error body wherever it sits in [text], such as an HTTP error. */
fun serverMessageOf(text: String?): String? =
    text?.let { SERVER_MESSAGE.find(it)?.groupValues?.get(1) }
