package org.saudigitus.semis.core.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.dhis2.commons.network.NetworkUtils
import org.hisp.dhis.android.core.D2
import org.saudigitus.semis.core.data.model.SyncTarget
import timber.log.Timber
import javax.inject.Inject

class SyncRepositoryImpl @Inject constructor(
    private val d2: D2,
    private val networkUtils: NetworkUtils,
) : SyncRepository {

    override suspend fun upload(targets: List<SyncTarget>): SyncOutcome =
        withContext(Dispatchers.IO) {
            // Trying without a connection only produces a failure the user already knows about,
            // and the records are no worse off for having waited.
            if (!networkUtils.isOnline()) return@withContext SyncOutcome.OFFLINE

            // A program that registers people carries its events under the enrollment, so sending
            // the people sends them too; a program of standalone events has to be sent as events.
            // Which of the two applies is what the screen said when it named the target.
            val sendsPeople = targets.any { it is SyncTarget.Tracker }
            val sendsEvents = targets.any { it is SyncTarget.Events }

            runCatching {
                if (sendsPeople) {
                    d2.trackedEntityModule().trackedEntityInstances().blockingUpload()
                }
                if (sendsEvents) {
                    d2.eventModule().events().blockingUpload()
                }
            }.fold(
                onSuccess = { SyncOutcome.SENT },
                onFailure = { error ->
                    // The records stay where they are and the periodic sync will try again, so
                    // this is worth recording but not worth interrupting anyone over.
                    Timber.tag(SYNC_TAG).e(error)
                    SyncOutcome.FAILED
                },
            )
        }

    private companion object {
        const val SYNC_TAG = "SEMIS_SYNC"
    }
}
