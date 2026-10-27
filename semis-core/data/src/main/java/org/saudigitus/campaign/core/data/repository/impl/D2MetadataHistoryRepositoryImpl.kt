package org.saudigitus.campaign.core.data.repository.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.hisp.dhis.android.core.D2
import org.saudigitus.campaign.core.data.dao.D2MetadataHistoryDao
import org.saudigitus.campaign.core.data.models.datastore.appconfig.DisplayMode
import org.saudigitus.campaign.core.data.models.entity.EventHistoryEntity
import org.saudigitus.campaign.core.data.models.entity.TrackedEntityInstanceHistoryEntity
import org.saudigitus.campaign.core.data.repository.D2MetadataHistoryRepository
import org.saudigitus.campaign.core.data.repository.DatastoreRepository
import org.saudigitus.campaign.core.utils.DateHelper

class D2MetadataHistoryRepositoryImpl(
    private val d2: D2,
    private val d2MetadataHistoryDao: D2MetadataHistoryDao,
    private val datastoreRepository: DatastoreRepository,
) : D2MetadataHistoryRepository {
    override suspend fun createTEIHistory(program: String, tei: String) =
        withContext(Dispatchers.IO) {
            d2MetadataHistoryDao.upsertTEIHistory(
                TrackedEntityInstanceHistoryEntity(
                    uid = tei,
                    program = program,
                    userId = d2.userModule().user().blockingGet()?.uid().orEmpty(),
                    date = DateHelper.formatDate(System.currentTimeMillis()).orEmpty()
                )
            )
        }

    override suspend fun createEventHistory(program: String, event: String) =
        withContext(Dispatchers.IO) {
            d2MetadataHistoryDao.upsertEventHistory(
                EventHistoryEntity(
                    uid = event,
                    program = program,
                    userId = d2.userModule().user().blockingGet()?.uid().orEmpty(),
                    date = DateHelper.formatDate(System.currentTimeMillis()).orEmpty()
                )
            )
        }

    override suspend fun getTEIHistory(program: String) = withContext(Dispatchers.IO) {
        val userId = d2.userModule().user().blockingGet()?.uid().orEmpty()
        d2MetadataHistoryDao.getTEIHistory(program, userId)
    }

    override suspend fun getTEIHistoryUids(todayOnly: Boolean) = withContext(Dispatchers.IO) {
        val userId = d2.userModule().user().blockingGet()?.uid().orEmpty()

        if (todayOnly) {
            d2MetadataHistoryDao.getTEIHistoryUidsByCurrentDate(userId)
        } else {
            d2MetadataHistoryDao.getTEIHistoryUids(userId)
        }
    }

    override suspend fun getTEIHistoryUids(program: String) = withContext(Dispatchers.IO) {
        val displayMode = getDisplayMode(program)
        val userId = d2.userModule().user().blockingGet()?.uid().orEmpty()

        when (displayMode?.listView) {
            "TODAY" -> d2MetadataHistoryDao.getTEIHistoryUidsByCurrentDate(program = program, userId = userId)
            else -> d2MetadataHistoryDao.getTEIHistoryUidsByProgram(program, userId)
        }
    }

    override suspend fun getEventHistoryUids(todayOnly: Boolean) = withContext(Dispatchers.IO) {
        val userId = d2.userModule().user().blockingGet()?.uid().orEmpty()

        if (todayOnly) {
            d2MetadataHistoryDao.getEventHistoryUidsByCurrentDate(userId)
        } else {
            d2MetadataHistoryDao.getEventHistoryUids(userId)
        }
    }

    override suspend fun getEventHistory(program: String) = withContext(Dispatchers.IO) {
        val userId = d2.userModule().user().blockingGet()?.uid().orEmpty()
        d2MetadataHistoryDao.getEventHistory(program, userId)
    }

    override suspend fun getEventHistoryUids(program: String) = withContext(Dispatchers.IO) {
        val displayMode = getDisplayMode(program)
        val userId = d2.userModule().user().blockingGet()?.uid().orEmpty()

        when (displayMode?.listView) {
            "TODAY" -> d2MetadataHistoryDao.getEventHistoryUidsByCurrentDate(program = program, userId = userId)
            else -> d2MetadataHistoryDao.getEventHistoryUidsByProgram(program, userId)
        }
    }

    override suspend fun cleanTable() = withContext(Dispatchers.IO) {
        d2MetadataHistoryDao.cleanTable()
    }

    private suspend fun getDisplayMode(program: String): DisplayMode? {
        return datastoreRepository.getAppConfig(program)?.default?.displayMode
    }
}
