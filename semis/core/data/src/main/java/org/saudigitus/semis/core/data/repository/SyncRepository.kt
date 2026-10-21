package org.saudigitus.semis.core.data.repository

import org.saudigitus.semis.core.data.model.SyncTarget

/** Outcome of an attempt to send, in the terms the person who saved would describe it. */
enum class SyncOutcome {

    /** The server has the records. */
    SENT,

    /** Nothing was attempted, because there was no connection to attempt it over. */
    OFFLINE,

    /** The attempt was made and did not go through. The records are still here. */
    FAILED,
}

interface SyncRepository {

    /**
     * Sends what is waiting for the given programs.
     *
     * What goes is decided by the records themselves, not by a list carried from the screen: the
     * SDK knows which of them the server has yet to see, so a save that happened while an earlier
     * attempt was running is not left behind.
     */
    suspend fun upload(targets: List<SyncTarget>): SyncOutcome
}
