package org.saudigitus.semis.core.data.repository

import org.saudigitus.semis.core.utils.Result

interface TeiDownloaderRepository {
    suspend fun downloadTei(
        ou: String,
        program: String,
        dataElementIds: List<String>,
        dataValues: List<String>,
    ): Result<Boolean, Exception>
}