package org.saudigitus.campaign.core.data.repository.impl

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.google.gson.GsonBuilder
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.Geometry
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.relationship.Relationship
import org.hisp.dhis.android.core.relationship.RelationshipItem
import org.hisp.dhis.android.core.relationship.RelationshipItemTrackedEntityInstance
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.saudigitus.campaign.core.data.models.LoggingPayload
import org.saudigitus.campaign.core.data.repository.LoggingRepository
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

private const val PNCM_LOGS_URL = "https://esifapi.saudigitus.org/api/pncmLogs"
private const val AGGREGATE_LOG_TYPE = "aggregate"

private enum class TrackerExportMode(
    val filePrefix: String,
    val dataType: String,
    val logError: String,
) {
    ALL_DATA(
        filePrefix = "all_teis_data",
        dataType = "ALL_DATA",
        logError = "ALL_TEIS_DATA",
    ),
    SYNCED(
        filePrefix = "synced_teis",
        dataType = "SYNCED",
        logError = "SYNCED_TEIS_DATA",
    ),
    NOT_SYNCED(
        filePrefix = "not_synced_teis",
        dataType = "NOT_SYNCED",
        logError = "Tracked Entity Instances with errors Exported",
    ),
}

class LoggingRepositoryImpl @Inject constructor(
    private val context: Context,
    private val d2: D2,
) : LoggingRepository {

    private val gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    explicitNulls = false
                },
                contentType = ContentType.Any,
            )
        }
        install(HttpRequestRetry) {
            retryOnException(5, true)
            exponentialDelay()
        }
    }

    override suspend fun exportAllTeis(): String? =
        exportTeis(TrackerExportMode.ALL_DATA)

    override suspend fun exportSyncedTeis(): String? =
        exportTeis(TrackerExportMode.SYNCED)

    override suspend fun exportNotSyncedTeis(): String? =
        exportTeis(TrackerExportMode.NOT_SYNCED)

    override suspend fun forceSyncAllTeis(): String? =
        sendTeis(TrackerExportMode.ALL_DATA)

    override suspend fun forceSyncSyncedTeis(): String? =
        sendTeis(TrackerExportMode.SYNCED)

    override suspend fun forceSyncNotSyncedTeis(): String? =
        sendTeis(TrackerExportMode.NOT_SYNCED)

    private suspend fun exportTeis(exportMode: TrackerExportMode): String? = withContext(Dispatchers.IO) {
        runCatching {
            val exportData = buildTeisExportData(exportMode)
            val fileName = "${exportMode.filePrefix}_${fileTimestamp()}.json"
            val jsonString = gson.toJson(exportData)
            val fileLocation = writeJsonToDownloads(fileName, jsonString)

            "TEIs exported successfully to: $fileLocation"
        }.getOrNull()
    }

    private suspend fun sendTeis(exportMode: TrackerExportMode): String? = withContext(Dispatchers.IO) {
        runCatching {
            val exportData = buildTeisExportData(exportMode)
            val username = exportData["user"] as? String ?: "unknown"
            val payload = LoggingPayload(
                error = exportMode.logError,
                type = "TEI",
                data = listOf(gson.toJson(exportData)),
                user = username,
                time = Date().toIsoString().orEmpty(),
                server = d2.systemInfoModule().systemInfo().blockingGet()?.contextPath().orEmpty(),
            )

            if (sendLog(payload, AGGREGATE_LOG_TYPE)) {
                "Dados enviados com sucesso."
            } else {
                null
            }
        }.getOrNull()
    }

    private fun buildTeisExportData(exportMode: TrackerExportMode): Map<String, Any?> {
        return when (exportMode) {
            TrackerExportMode.ALL_DATA -> buildAllTeisExportData()
            TrackerExportMode.SYNCED -> buildSyncedTeisExportData()
            TrackerExportMode.NOT_SYNCED -> buildNotSyncedTeisExportData()
        }
    }

    private fun buildNotSyncedTeisExportData(): Map<String, Any?> {
        val notSyncedStates = notSyncedStates()
        val notSyncedTeis = getNotSyncedTrackedEntityInstances(notSyncedStates)
        val notSyncedEnrollments = getNotSyncedEnrollments(notSyncedStates)
        val notSyncedEvents = getNotSyncedEvents(notSyncedStates)
        val notSyncedRelationships = getNotSyncedRelationships(notSyncedStates)
        val enrollmentsFromEvents = getEnrollmentsByUid(
            notSyncedEvents
                .mapNotNull { it.enrollment() }
                .distinct(),
        )

        val teiUids = (
            notSyncedTeis.map { it.uid() } +
                notSyncedEnrollments.mapNotNull { it.trackedEntityInstance() } +
                enrollmentsFromEvents.mapNotNull { it.trackedEntityInstance() } +
                notSyncedRelationships.flatMap { it.teiUids() }
            )
            .filter { it.isNotBlank() }
            .distinct()

        val trackedEntityInstances = getTrackedEntityInstancesByUid(teiUids)
        val trackedEntityInstanceUids = trackedEntityInstances.map { it.uid() }
        val enrollments = getEnrollmentsByTei(trackedEntityInstanceUids).distinctBy { it.uid() }
        val enrollmentUids = enrollments.map { it.uid() }
        val events = getEventsByEnrollment(enrollmentUids).distinctBy { it.uid() }
        val relationshipsByTei = trackedEntityInstanceUids.associateWith { getRelationshipsByTei(it) }
        val enrollmentsByTei = enrollments.groupBy { it.trackedEntityInstance().orEmpty() }
        val eventsByEnrollment = events.groupBy { it.enrollment().orEmpty() }
        val username = d2.userModule().user().blockingGet()?.username() ?: "unknown"

        return linkedMapOf(
            "exportDate" to Date().toIsoString(),
            "dataType" to TrackerExportMode.NOT_SYNCED.dataType,
            "totalCount" to trackedEntityInstances.size,
            "user" to username,
            "trackedEntityInstances" to trackedEntityInstances.map { tei ->
                tei.toExportPayload(
                    enrollments = enrollmentsByTei[tei.uid()].orEmpty(),
                    eventsByEnrollment = eventsByEnrollment,
                    relationships = relationshipsByTei[tei.uid()].orEmpty(),
                )
            },
        )
    }

    private fun buildSyncedTeisExportData(): Map<String, Any?> {
        val trackedEntityInstances = d2.trackedEntityModule()
            .trackedEntityInstances()
            .byAggregatedSyncState().`in`(syncedStates())
            .byDeleted().isFalse
            .withTrackedEntityAttributeValues()
            .blockingGet()

        return buildTeisExportPayload(
            exportMode = TrackerExportMode.SYNCED,
            trackedEntityInstances = trackedEntityInstances,
        )
    }

    private fun buildAllTeisExportData(): Map<String, Any?> {
        val trackedEntityInstances = d2.trackedEntityModule()
            .trackedEntityInstances()
            .byDeleted().isFalse
            .withTrackedEntityAttributeValues()
            .blockingGet()

        return buildTeisExportPayload(
            exportMode = TrackerExportMode.ALL_DATA,
            trackedEntityInstances = trackedEntityInstances,
        )
    }

    private fun buildTeisExportPayload(
        exportMode: TrackerExportMode,
        trackedEntityInstances: List<TrackedEntityInstance>,
    ): Map<String, Any?> {
        val teiUids = trackedEntityInstances.map { it.uid() }
        val enrollments = getEnrollmentsByTei(teiUids).distinctBy { it.uid() }
        val events = getEventsByEnrollment(enrollments.map { it.uid() }).distinctBy { it.uid() }

        val relationshipsByTei = teiUids.associateWith { getRelationshipsByTei(it) }
        val enrollmentsByTei = enrollments.groupBy { it.trackedEntityInstance().orEmpty() }
        val eventsByEnrollment = events.groupBy { it.enrollment().orEmpty() }
        val username = d2.userModule().user().blockingGet()?.username() ?: "unknown"

        return linkedMapOf(
            "exportDate" to Date().toIsoString(),
            "dataType" to exportMode.dataType,
            "totalCount" to trackedEntityInstances.size,
            "totalEnrollmentCount" to enrollments.size,
            "totalEventCount" to events.size,
            "user" to username,
            "trackedEntityInstances" to trackedEntityInstances.map { tei ->
                tei.toExportPayload(
                    enrollments = enrollmentsByTei[tei.uid()].orEmpty(),
                    eventsByEnrollment = eventsByEnrollment,
                    relationships = relationshipsByTei[tei.uid()].orEmpty(),
                )
            },
        )
    }

    private suspend fun sendLog(body: LoggingPayload, type: String): Boolean {
        val response = httpClient.post("$PNCM_LOGS_URL/$type") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }

        return response.status.value in 200..299
    }

    private fun notSyncedStates(): List<State> = listOf(
        State.TO_POST,
        State.TO_UPDATE,
        State.UPLOADING,
        State.ERROR,
        State.WARNING,
    )

    private fun syncedStates(): List<State> = listOf(
        State.SYNCED,
        State.SYNCED_VIA_SMS,
    )

    private fun getNotSyncedTrackedEntityInstances(
        states: List<State>,
    ): List<TrackedEntityInstance> {
        return d2.trackedEntityModule()
            .trackedEntityInstances()
            .byAggregatedSyncState().`in`(states)
            .byDeleted().isFalse
            .withTrackedEntityAttributeValues()
            .blockingGet()
    }

    private fun getTrackedEntityInstancesByUid(
        teiUids: List<String>,
    ): List<TrackedEntityInstance> {
        return if (teiUids.isEmpty()) {
            emptyList()
        } else {
            d2.trackedEntityModule()
                .trackedEntityInstances()
                .byUid().`in`(teiUids)
                .byDeleted().isFalse
                .withTrackedEntityAttributeValues()
                .blockingGet()
        }
    }

    private fun getNotSyncedEnrollments(states: List<State>): List<Enrollment> {
        return d2.enrollmentModule()
            .enrollments()
            .byAggregatedSyncState().`in`(states)
            .byDeleted().isFalse
            .blockingGet()
    }

    private fun getNotSyncedEvents(states: List<State>): List<Event> {
        return d2.eventModule()
            .events()
            .byAggregatedSyncState().`in`(states)
            .byDeleted().isFalse
            .blockingGet()
    }

    private fun getNotSyncedRelationships(states: List<State>): List<Relationship> {
        return d2.relationshipModule()
            .relationships()
            .bySyncState().`in`(states)
            .byDeleted().isFalse
            .withItems()
            .blockingGet()
    }

    private fun getEnrollmentsByUid(enrollmentUids: List<String>): List<Enrollment> {
        return if (enrollmentUids.isEmpty()) {
            emptyList()
        } else {
            d2.enrollmentModule()
                .enrollments()
                .byUid().`in`(enrollmentUids)
                .byDeleted().isFalse
                .blockingGet()
        }
    }

    private fun getEnrollmentsByTei(teiUids: List<String>): List<Enrollment> {
        return if (teiUids.isEmpty()) {
            emptyList()
        } else {
            d2.enrollmentModule()
                .enrollments()
                .byTrackedEntityInstance().`in`(teiUids)
                .byDeleted().isFalse
                .blockingGet()
        }
    }

    private fun getEventsByEnrollment(enrollmentUids: List<String>): List<Event> {
        return if (enrollmentUids.isEmpty()) {
            emptyList()
        } else {
            d2.eventModule()
                .events()
                .byEnrollmentUid().`in`(enrollmentUids)
                .byDeleted().isFalse
                .withTrackedEntityDataValues()
                .blockingGet()
        }
    }

    private fun getRelationshipsByTei(teiUid: String): List<Relationship> {
        return d2.relationshipModule()
            .relationships()
            .getByItem(
                searchItem = RelationshipItem.builder()
                    .trackedEntityInstance(
                        RelationshipItemTrackedEntityInstance.builder()
                            .trackedEntityInstance(teiUid)
                            .build()
                    )
                    .build(),
                includeDeleted = false,
                onlyAccessible = false,
            )
    }

    private fun TrackedEntityInstance.toExportPayload(
        enrollments: List<Enrollment>,
        eventsByEnrollment: Map<String, List<Event>>,
        relationships: List<Relationship>,
    ): Map<String, Any?> {
        return linkedMapOf(
            "uid" to uid(),
            "trackedEntity" to uid(),
            "trackedEntityInstance" to uid(),
            "created" to created().toIsoString(),
            "lastUpdated" to lastUpdated().toIsoString(),
            "createdAtClient" to createdAtClient().toIsoString(),
            "lastUpdatedAtClient" to lastUpdatedAtClient().toIsoString(),
            "organisationUnit" to organisationUnit(),
            "orgUnit" to organisationUnit(),
            "trackedEntityType" to trackedEntityType(),
            "geometry" to geometry().toExportPayload(),
            "syncState" to syncState()?.name,
            "aggregatedSyncState" to aggregatedSyncState()?.name,
            "deleted" to deleted(),
            "attributes" to trackedEntityAttributeValues().orEmpty().map { it.toExportPayload() },
            "enrollments" to enrollments.map {
                it.toExportPayload(eventsByEnrollment[it.uid()].orEmpty())
            },
            "relationships" to relationships.map { it.toExportPayload() },
        )
    }

    private fun Enrollment.toExportPayload(events: List<Event>): Map<String, Any?> {
        return linkedMapOf(
            "uid" to uid(),
            "enrollment" to uid(),
            "created" to created().toIsoString(),
            "lastUpdated" to lastUpdated().toIsoString(),
            "createdAtClient" to createdAtClient().toIsoString(),
            "lastUpdatedAtClient" to lastUpdatedAtClient().toIsoString(),
            "organisationUnit" to organisationUnit(),
            "orgUnit" to organisationUnit(),
            "program" to program(),
            "trackedEntity" to trackedEntityInstance(),
            "trackedEntityInstance" to trackedEntityInstance(),
            "enrollmentDate" to enrollmentDate().toIsoString(),
            "enrolledAt" to enrollmentDate().toIsoString(),
            "incidentDate" to incidentDate().toIsoString(),
            "occurredAt" to incidentDate().toIsoString(),
            "completedDate" to completedDate().toIsoString(),
            "completedAt" to completedDate().toIsoString(),
            "followUp" to followUp(),
            "status" to status()?.name,
            "geometry" to geometry().toExportPayload(),
            "syncState" to syncState()?.name,
            "aggregatedSyncState" to aggregatedSyncState()?.name,
            "deleted" to deleted(),
            "events" to events.map { it.toExportPayload() },
        )
    }

    private fun Event.toExportPayload(): Map<String, Any?> {
        return linkedMapOf(
            "uid" to uid(),
            "event" to uid(),
            "created" to created().toIsoString(),
            "lastUpdated" to lastUpdated().toIsoString(),
            "createdAtClient" to createdAtClient().toIsoString(),
            "lastUpdatedAtClient" to lastUpdatedAtClient().toIsoString(),
            "enrollment" to enrollment(),
            "program" to program(),
            "programStage" to programStage(),
            "organisationUnit" to organisationUnit(),
            "orgUnit" to organisationUnit(),
            "eventDate" to eventDate().toIsoString(),
            "occurredAt" to eventDate().toIsoString(),
            "dueDate" to dueDate().toIsoString(),
            "scheduledAt" to dueDate().toIsoString(),
            "completedDate" to completedDate().toIsoString(),
            "completedAt" to completedDate().toIsoString(),
            "completedBy" to completedBy(),
            "status" to status()?.name,
            "attributeOptionCombo" to attributeOptionCombo(),
            "assignedUser" to assignedUser(),
            "geometry" to geometry().toExportPayload(),
            "syncState" to syncState()?.name,
            "aggregatedSyncState" to aggregatedSyncState()?.name,
            "deleted" to deleted(),
            "dataValues" to trackedEntityDataValues().orEmpty().map { it.toExportPayload() },
        )
    }

    private fun TrackedEntityAttributeValue.toExportPayload(): Map<String, Any?> {
        return linkedMapOf(
            "attribute" to trackedEntityAttribute(),
            "trackedEntityAttribute" to trackedEntityAttribute(),
            "trackedEntityInstance" to trackedEntityInstance(),
            "value" to value(),
            "created" to created().toIsoString(),
            "lastUpdated" to lastUpdated().toIsoString(),
        )
    }

    private fun TrackedEntityDataValue.toExportPayload(): Map<String, Any?> {
        return linkedMapOf(
            "event" to event(),
            "dataElement" to dataElement(),
            "value" to value(),
            "storedBy" to storedBy(),
            "providedElsewhere" to providedElsewhere(),
            "created" to created().toIsoString(),
            "lastUpdated" to lastUpdated().toIsoString(),
        )
    }

    private fun Relationship.toExportPayload(): Map<String, Any?> {
        return linkedMapOf(
            "uid" to uid(),
            "relationship" to uid(),
            "name" to name(),
            "relationshipType" to relationshipType(),
            "from" to from().toExportPayload(),
            "to" to to().toExportPayload(),
            "syncState" to syncState()?.name,
            "deleted" to deleted(),
            "created" to created().toIsoString(),
            "lastUpdated" to lastUpdated().toIsoString(),
        )
    }

    private fun Relationship.teiUids(): List<String> {
        return listOfNotNull(
            from()?.trackedEntityInstance()?.trackedEntityInstance(),
            to()?.trackedEntityInstance()?.trackedEntityInstance(),
        )
    }

    private fun RelationshipItem?.toExportPayload(): Map<String, Any?>? {
        return this?.let {
            linkedMapOf(
                "elementUid" to it.elementUid(),
                "elementType" to it.elementType(),
                "relationshipItemType" to it.relationshipItemType()?.name,
                "trackedEntityInstance" to it.trackedEntityInstance()?.trackedEntityInstance(),
                "enrollment" to it.enrollment()?.enrollment(),
                "event" to it.event()?.event(),
            )
        }
    }

    private fun Geometry?.toExportPayload(): Map<String, Any?>? {
        return this?.let {
            linkedMapOf(
                "type" to it.type()?.name,
                "coordinates" to it.coordinates(),
            )
        }
    }

    private fun Date?.toIsoString(): String? {
        return this?.let { isoDateFormat().format(it) }
    }

    private fun isoDateFormat() = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        .apply { timeZone = TimeZone.getTimeZone("UTC") }

    private fun fileTimestamp(): String =
        SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss.SSS'Z'", Locale.US)
            .apply { timeZone = TimeZone.getTimeZone("UTC") }
            .format(Date())

    private fun writeJsonToDownloads(fileName: String, jsonString: String): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            writeJsonToDownloadsWithMediaStore(fileName, jsonString)
        } else {
            writeJsonToDownloadsWithFile(fileName, jsonString)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun writeJsonToDownloadsWithMediaStore(fileName: String, jsonString: String): String {
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "application/json")
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            put(MediaStore.Downloads.IS_PENDING, 1)
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            ?: error("Could not create TEI export file")

        resolver.openOutputStream(uri)?.bufferedWriter()?.use { writer ->
            writer.write(jsonString)
        } ?: error("Could not write TEI export file")

        contentValues.clear()
        contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
        resolver.update(uri, contentValues, null, null)

        return "${Environment.DIRECTORY_DOWNLOADS}/$fileName"
    }

    @Suppress("DEPRECATION")
    private fun writeJsonToDownloadsWithFile(fileName: String, jsonString: String): String {
        val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!directory.exists()) {
            directory.mkdirs()
        }

        val file = File(directory, fileName)
        file.writeText(jsonString)

        return file.absolutePath
    }
}
