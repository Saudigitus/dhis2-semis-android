package org.saudigitus.semis.core.data.model.app_config


import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Transfer(
    @SerialName("approvedCode")
    val approvedCode: String?,
    @SerialName("destinySchool")
    val destinySchool: String?,
    @SerialName("enabled")
    val enabled: Boolean?,
    @SerialName("lastUpdate")
    val lastUpdate: String?,
    @SerialName("penddingCode")
    @JsonNames("pendingCode")
    val pendingCode: String?,
    @SerialName("programStage")
    val programStage: String?,
    @SerialName("reprovedCode")
    val reprovedCode: String?,
    @SerialName("status")
    val status: String?,
    @SerialName("statusOptions")
    val statusOptions: List<StatusOption>?
)

/**
 * Code of the configured transfer status carrying [key], such as pending, approved or
 * rejected. The keys are configuration driven, so they are matched leniently.
 */
fun Transfer.statusCodeFor(key: String): String? = statusOptions
    ?.firstOrNull { option ->
        option.key?.trim()?.equals(key, ignoreCase = true) == true
    }
    ?.code
    ?.takeIf(String::isNotBlank)

fun Transfer.pendingStatusCode(): String? = statusCodeFor(PENDING_STATUS_KEY)

/**
 * Code written on a transfer the destination school accepted. Falls back to the legacy
 * top level property the status option points at through its config key.
 */
fun Transfer.approvedStatusCode(): String? =
    statusCodeFor(APPROVED_STATUS_KEY) ?: approvedCode?.takeIf(String::isNotBlank)

/**
 * Code written on a transfer the destination school refused. Configurations name its option
 * rejected, older ones reproved, and the oldest only carry the top level property.
 */
fun Transfer.rejectedStatusCode(): String? =
    statusCodeFor(REJECTED_STATUS_KEY)
        ?: statusCodeFor(LEGACY_REJECTED_STATUS_KEY)
        ?: reprovedCode?.takeIf(String::isNotBlank)

private const val PENDING_STATUS_KEY = "pending"
private const val APPROVED_STATUS_KEY = "approved"
private const val REJECTED_STATUS_KEY = "rejected"
private const val LEGACY_REJECTED_STATUS_KEY = "reproved"

/**
 * Whether transfers can be listed and raised at all.
 *
 * The origin school is not part of the configuration: the transfer program stage holds a
 * single organisation unit data element and it carries the destination, while the origin is
 * the organisation unit the request event belongs to. A datastore that still carries an
 * originSchool key loads as before and the key is ignored.
 */
fun Transfer?.isTransferEnabledAndConfigured(): Boolean = this?.run {
    enabled == true &&
        !programStage.isNullOrBlank() &&
        !destinySchool.isNullOrBlank()
} == true
