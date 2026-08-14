package org.saudigitus.campaign.core.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import org.saudigitus.campaign.core.data.models.entity.EventHistoryEntity
import org.saudigitus.campaign.core.data.models.entity.TrackedEntityInstanceHistoryEntity
import org.saudigitus.campaign.core.utils.DateHelper

@Dao
interface D2MetadataHistoryDao {
    @Upsert
    suspend fun upsertTEIHistory(tei: TrackedEntityInstanceHistoryEntity)

    @Upsert
    suspend fun upsertEventHistory(event: EventHistoryEntity)

    @Query("SELECT * FROM trackedentityinstancehistoryentity WHERE program = :program AND userId = :userId AND date = :date")
    suspend fun getTEIHistory(
        program: String,
        userId: String,
        date: String = DateHelper.formatDate(System.currentTimeMillis()).orEmpty()
    ): List<TrackedEntityInstanceHistoryEntity>

    @Query("SELECT uid FROM trackedentityinstancehistoryentity WHERE program = :program AND userId = :userId AND date = :date")
    suspend fun getTEIHistoryUids(
        program: String,
        userId: String,
        date: String = DateHelper.formatDate(System.currentTimeMillis()).orEmpty()
    ): List<String>


    @Query("SELECT uid FROM trackedentityinstancehistoryentity WHERE program = :program AND userId = :userId AND date = :date")
    suspend fun getTEIHistoryUidsByCurrentDate(
        program: String,
        userId: String,
        date: String = DateHelper.formatDate(System.currentTimeMillis()).orEmpty()
    ): List<String>

    @Query("SELECT uid FROM trackedentityinstancehistoryentity WHERE program = :program AND userId = :userId")
    suspend fun getTEIHistoryUidsByProgram(
        program: String,
        userId: String
    ): List<String>

    @Query("SELECT uid FROM trackedentityinstancehistoryentity WHERE userId = :userId AND date = :date")
    suspend fun getTEIHistoryUidsByCurrentDate(
        userId: String,
        date: String = DateHelper.formatDate(System.currentTimeMillis()).orEmpty()
    ): List<String>

    @Query("SELECT uid FROM trackedentityinstancehistoryentity WHERE userId = :userId")
    suspend fun getTEIHistoryUids(userId: String): List<String>

    @Query("SELECT * FROM eventhistoryentity WHERE program = :program AND userId = :userId AND date = :date")
    suspend fun getEventHistory(
        program: String,
        userId: String,
        date: String = DateHelper.formatDate(System.currentTimeMillis()).orEmpty()
    ): List<EventHistoryEntity>

    @Query("SELECT uid FROM eventhistoryentity WHERE program = :program AND userId = :userId AND date = :date")
    suspend fun getEventHistoryUids(
        program: String,
        userId: String,
        date: String = DateHelper.formatDate(System.currentTimeMillis()).orEmpty()
    ): List<String>

    @Query("SELECT uid FROM eventhistoryentity WHERE program = :program AND userId = :userId AND date = :date")
    suspend fun getEventHistoryUidsByCurrentDate(
        program: String,
        userId: String,
        date: String = DateHelper.formatDate(System.currentTimeMillis()).orEmpty()
    ): List<String>

    @Query("SELECT uid FROM eventhistoryentity WHERE program = :program AND userId = :userId")
    suspend fun getEventHistoryUidsByProgram(
        program: String,
        userId: String
    ): List<String>

    @Query("SELECT uid FROM eventhistoryentity WHERE userId = :userId AND date = :date")
    suspend fun getEventHistoryUidsByCurrentDate(
        userId: String,
        date: String = DateHelper.formatDate(System.currentTimeMillis()).orEmpty()
    ): List<String>


    @Query("SELECT uid FROM eventhistoryentity WHERE userId = :userId")
    suspend fun getEventHistoryUids(userId: String): List<String>

    @Query("DELETE FROM trackedentityinstancehistoryentity")
    suspend fun cleanTable()
}
