package org.saudigitus.semis.core.data.model.app_config


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * What the deployment asks of the mobile app, as the `android` key of the SEMIS datastore states
 * it.
 *
 * Kept apart from the per program configuration because these are answers about the device rather
 * than about a programme: how a phone behaves after a save is the same whether the teacher is
 * capturing learners or staff.
 */
@Serializable
data class AndroidSettings(
    @SerialName("syncMode")
    val syncMode: String?,
)

/**
 * What the app does once a record has been written.
 *
 * The right answer differs between deployments and the app is in no position to pick: a school on
 * a steady connection wants the records gone without being asked, one that is offline all morning
 * is only interrupted by a question it cannot answer, and a deployment content to let the periodic
 * sync carry everything wants neither.
 */
enum class SyncMode {

    /** Nothing happens beyond the save. The periodic sync and the manual button carry the records. */
    DEFAULT,

    /** The app offers to send and the user decides. */
    PROMPT,

    /** The app sends without asking, when there is a connection to send over. */
    AUTO,
}

/**
 * Reads the configured mode, and answers DEFAULT to everything else.
 *
 * A missing key, an empty one, a value this version does not know: all of them mean the deployment
 * has not asked for anything, and none of them is a reason to stand between a teacher and the
 * record they are capturing. This is a preference about sending, not a precondition for capturing.
 */
fun syncModeOf(value: String?): SyncMode =
    SyncMode.entries.firstOrNull { it.name.equals(value?.trim(), ignoreCase = true) }
        ?: SyncMode.DEFAULT
