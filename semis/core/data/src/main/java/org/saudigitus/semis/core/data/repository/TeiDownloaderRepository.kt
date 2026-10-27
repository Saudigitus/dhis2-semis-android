package org.saudigitus.semis.core.data.repository

import org.saudigitus.semis.core.utils.Result

interface TeiDownloaderRepository {
    /**
     * Brings the learners of a class down to the device, answering how many arrived.
     *
     * The number is what lets the screen tell the user that something happened, which a plain
     * success cannot: a download that works looks exactly like one that was never pressed.
     */
    suspend fun downloadTei(
        ou: String,
        program: String,
        dataElementIds: List<String>,
        dataValues: List<String>,
    ): Result<Int, Exception>
}