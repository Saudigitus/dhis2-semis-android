package org.saudigitus.semis.core.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.dhis2.commons.network.NetworkUtils
import org.dhis2.commons.resources.ResourceManager
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitMode
import org.saudigitus.semis.core.data.R
import org.saudigitus.semis.core.data.model.app_config.SEMISConfigItem
import org.saudigitus.semis.core.data.model.app_config.Transfer
import org.saudigitus.semis.core.data.model.app_config.approvedStatusCode
import org.saudigitus.semis.core.data.model.app_config.isTransferEnabledAndConfigured
import org.saudigitus.semis.core.data.model.app_config.pendingStatusCode
import org.saudigitus.semis.core.data.model.app_config.rejectedStatusCode
import org.saudigitus.semis.core.data.model.transfer.TeiTransfer
import org.saudigitus.semis.core.data.model.transfer.TeiTransferFailure
import org.saudigitus.semis.core.data.model.transfer.TeiTransferRecord
import org.saudigitus.semis.core.data.model.transfer.TeiTransferMetadata
import org.saudigitus.semis.core.data.model.transfer.TeiTransferRequest
import org.saudigitus.semis.core.data.model.transfer.TeiTransferResult
import org.saudigitus.semis.core.data.model.transfer.TransferDecision
import org.saudigitus.semis.core.data.model.transfer.TransferStatus
import org.saudigitus.semis.core.data.model.transfer.learnerIdentity
import org.saudigitus.semis.core.data.model.transfer.transferStatusOf
import org.saudigitus.semis.core.data.model.transfer.transferableProgramStages
import org.saudigitus.semis.core.data.utils.Transformations
import org.saudigitus.semis.core.utils.DateHelper
import java.util.Date
import javax.inject.Inject

class TeiTransferRepositoryImpl @Inject constructor(
    private val d2: D2,
    private val appConfigRepository: AppConfigRepository,
    private val eventRepository: EventRepository,
    private val transformations: Transformations,
    private val networkUtils: NetworkUtils,
    private val resourceManager: ResourceManager,
) : TeiTransferRepository {

    override suspend fun getTransferMetadata(
        program: String,
    ): TeiTransferMetadata = withContext(Dispatchers.IO) {
        val transfer = appConfigRepository.getAppConfig(program)
            ?.transfer
            .requireConfigured()

        TeiTransferMetadata(
            programStage = transfer.programStage.orEmpty(),
            destinationSchoolDataElement = transfer.destinySchool.orEmpty(),
            statusDataElement = transfer.status.orEmpty(),
            pendingStatusCode = transfer.pendingStatusCode().orEmpty(),
        )
    }

    override suspend fun requestTransfer(
        request: TeiTransferRequest,
    ): TeiTransferResult = withContext(Dispatchers.IO) {
        require(request.records.isNotEmpty()) {
            resourceManager.getString(R.string.transfer_record_required)
        }
        require(request.destinationOrgUnit.isNotBlank()) {
            resourceManager.getString(R.string.transfer_destination_required)
        }

        val transfer = appConfigRepository.getAppConfig(request.program)
            ?.transfer
            .requireConfigured()
        val requested = mutableListOf<String>()
        val failures = mutableListOf<TeiTransferFailure>()

        request.records.forEach { learner ->
            runCatching {
                requestLearnerTransfer(request, learner, transfer)
            }.onSuccess {
                requested += learner.teiUid
            }.onFailure { error ->
                failures += TeiTransferFailure(
                    teiUid = learner.teiUid,
                    message = error.message
                        ?: resourceManager.getString(R.string.transfer_failed),
                )
            }
        }

        TeiTransferResult(
            transferredTeiUids = requested,
            failures = failures,
        )
    }

    override suspend fun getOutgoingTransfers(
        program: String,
        currentOrgUnit: String,
    ): List<TeiTransfer> = withContext(Dispatchers.IO) {
        val config = enabledConfig(program) ?: return@withContext emptyList()
        val transfer = config.transfer ?: return@withContext emptyList()

        transferEvents(program, transfer)
            .byOrganisationUnitUid().eq(currentOrgUnit)
            .blockingGet()
            .asSequence()
            .mapNotNull { event -> teiTransfer(event, program, config, transfer) }
            .sortedByDescending { it.requestedAt }
            .toList()
    }

    override suspend fun getIncomingTransfers(
        program: String,
        currentOrgUnit: String,
    ): List<TeiTransfer> = withContext(Dispatchers.IO) {
        val config = enabledConfig(program) ?: return@withContext emptyList()
        val transfer = config.transfer ?: return@withContext emptyList()

        transferEvents(program, transfer)
            .blockingGet()
            .asSequence()
            .filter { event -> event.organisationUnit() != currentOrgUnit }
            .filter { event -> event.dataValue(transfer.destinySchool) == currentOrgUnit }
            .mapNotNull { event -> teiTransfer(event, program, config, transfer) }
            .sortedByDescending { it.requestedAt }
            .toList()
    }

    override suspend fun downloadIncomingTransfers(
        program: String,
        currentOrgUnit: String,
    ): Int = withContext(Dispatchers.IO) {
        val transfer = enabledConfig(program)?.transfer ?: return@withContext 0
        val destinationDataElement = transfer.destinySchool
            ?.takeIf { it.isNotBlank() }
            ?: return@withContext 0

        if (!networkUtils.isOnline()) {
            error(resourceManager.getString(R.string.error_no_internet_connection))
        }

        val teiUids = runCatching {
            d2.trackedEntityModule().trackedEntitySearch()
                .onlineFirst()
                .allowOnlineCache().eq(true)
                .byOrgUnitMode().eq(OrganisationUnitMode.ACCESSIBLE)
                .byProgram().eq(program)
                .byDataValue(destinationDataElement).eq(currentOrgUnit)
                .blockingGet()
                .mapNotNull { it.uid() }
                .distinct()
        }.getOrElse { error ->
            error(
                resourceManager.getString(
                    R.string.transfer_incoming_download_failed,
                    error.message.orEmpty(),
                ),
            )
        }

        if (teiUids.isEmpty()) return@withContext 0

        d2.trackedEntityModule().trackedEntityInstanceDownloader()
            .byUid().`in`(teiUids)
            .byProgramUid(program)
            .blockingDownload()

        teiUids.size
    }

    override suspend fun decideIncomingTransfer(
        program: String,
        currentOrgUnit: String,
        eventUid: String,
        decision: TransferDecision,
    ) = withContext(Dispatchers.IO) {
        val config = appConfigRepository.getAppConfig(program)
        val transfer = config?.transfer.requireConfigured()
        val event = d2.eventModule().events()
            .byUid().eq(eventUid)
            .withTrackedEntityDataValues()
            .one()
            .blockingGet()
            ?: error(resourceManager.getString(R.string.transfer_request_not_found))

        require(event.program() == program) {
            resourceManager.getString(R.string.transfer_wrong_program)
        }
        require(event.programStage() == transfer.programStage) {
            resourceManager.getString(R.string.transfer_wrong_program_stage)
        }
        require(event.dataValue(transfer.destinySchool) == currentOrgUnit) {
            resourceManager.getString(R.string.transfer_not_destination)
        }
        require(
            transfer.transferStatusOf(event.dataValue(transfer.status)) == TransferStatus.PENDING,
        ) {
            resourceManager.getString(R.string.transfer_not_pending)
        }

        val statusCode = when (decision) {
            TransferDecision.APPROVE -> transfer.approvedStatusCode()
                ?: error(resourceManager.getString(R.string.transfer_approved_status_missing))

            TransferDecision.REJECT -> transfer.rejectedStatusCode()
                ?: error(resourceManager.getString(R.string.transfer_rejected_status_missing))
        }

        if (decision == TransferDecision.APPROVE) {
            handOverLearner(event, program, currentOrgUnit, config)
        }

        d2.trackedEntityModule().trackedEntityDataValues()
            .value(eventUid, transfer.status.orEmpty())
            .blockingSet(statusCode)

        eventRepository.setEventStatus(eventUid, EventStatus.COMPLETED)
    }

    /**
     * Hands the learner over to the school that approved the request: the ownership
     * first, because it is the operation that can be refused, and only then the
     * enrollment and the history. The request event itself is left where it was raised.
     */
    private fun handOverLearner(
        event: Event,
        program: String,
        destinationOrgUnit: String,
        config: SEMISConfigItem?,
    ) {
        val enrollmentUid = event.enrollment()
            ?: error(resourceManager.getString(R.string.transfer_request_not_found))
        val enrollment = d2.enrollmentModule().enrollments()
            .uid(enrollmentUid)
            .blockingGet()
            ?: error(
                resourceManager.getString(
                    R.string.transfer_enrollment_not_found,
                    enrollmentUid,
                ),
            )
        val teiUid = enrollment.trackedEntityInstance()
            ?: error(resourceManager.getString(R.string.transfer_request_not_found))

        d2.trackedEntityModule().ownershipManager()
            .blockingTransfer(teiUid, program, destinationOrgUnit)

        d2.enrollmentModule().enrollments()
            .uid(enrollmentUid)
            .setOrganisationUnitUid(destinationOrgUnit)

        moveLearnerHistory(
            enrollmentUid = enrollmentUid,
            destinationOrgUnit = destinationOrgUnit,
            stages = transferableProgramStages(config),
        )
    }

    /**
     * Moves the events that describe the learner rather than the school, so that
     * reports by organisation unit agree with where the learner now is.
     */
    private fun moveLearnerHistory(
        enrollmentUid: String,
        destinationOrgUnit: String,
        stages: List<String>,
    ) {
        if (stages.isEmpty()) return

        d2.eventModule().events()
            .byEnrollmentUid().eq(enrollmentUid)
            .byProgramStageUid().`in`(stages)
            .byDeleted().isFalse
            .blockingGetUids()
            .forEach { uid ->
                d2.eventModule().events()
                    .uid(uid)
                    .setOrganisationUnitUid(destinationOrgUnit)
            }
    }

    /**
     * Raises the request on the school the learner currently belongs to, carrying only
     * the destination and the pending status. Nothing else moves: the learner stays
     * where they are until the destination approves.
     */
    private suspend fun requestLearnerTransfer(
        request: TeiTransferRequest,
        learner: TeiTransferRecord,
        transfer: Transfer,
    ) {
        val enrollment = d2.enrollmentModule().enrollments()
            .uid(learner.enrollmentUid)
            .blockingGet()
            ?: error(
                resourceManager.getString(
                    R.string.transfer_enrollment_not_found,
                    learner.enrollmentUid,
                ),
            )
        val originOrgUnit = enrollment.organisationUnit()
            ?: error(resourceManager.getString(R.string.transfer_enrollment_school_missing))

        require(originOrgUnit != request.destinationOrgUnit) {
            resourceManager.getString(R.string.transfer_same_school)
        }
        require(!hasPendingRequest(learner.enrollmentUid, transfer)) {
            resourceManager.getString(R.string.transfer_already_pending)
        }

        eventRepository.createEvent(
            orgUnit = originOrgUnit,
            program = request.program,
            programStage = transfer.programStage.orEmpty(),
            enrollmentUid = learner.enrollmentUid,
            data = listOf(
                transfer.destinySchool.orEmpty() to request.destinationOrgUnit,
                transfer.status.orEmpty() to transfer.pendingStatusCode(),
            ),
            eventDate = DateHelper.formatDate(request.effectiveDate.time),
            status = EventStatus.ACTIVE,
        )
    }

    /** A learner already awaiting a decision must not be requested a second time. */
    private fun hasPendingRequest(enrollmentUid: String, transfer: Transfer): Boolean =
        d2.eventModule().events()
            .byEnrollmentUid().eq(enrollmentUid)
            .byProgramStageUid().eq(transfer.programStage.orEmpty())
            .byDeleted().isFalse
            .withTrackedEntityDataValues()
            .blockingGet()
            .any { event ->
                transfer.transferStatusOf(event.dataValue(transfer.status)) ==
                    TransferStatus.PENDING
            }

    private suspend fun enabledConfig(program: String): SEMISConfigItem? =
        appConfigRepository.getAppConfig(program)
            ?.takeIf { it.transfer.isTransferEnabledAndConfigured() }

    private fun transferEvents(program: String, transfer: Transfer) =
        d2.eventModule().events()
            .byProgramUid().eq(program)
            .byProgramStageUid().eq(transfer.programStage.orEmpty())
            .byDeleted().isFalse
            .withTrackedEntityDataValues()

    private fun teiTransfer(
        event: Event,
        program: String,
        config: SEMISConfigItem,
        transfer: Transfer,
    ): TeiTransfer? {
        val enrollmentUid = event.enrollment() ?: return null
        val enrollment = d2.enrollmentModule().enrollments()
            .uid(enrollmentUid)
            .blockingGet()
            ?: return null
        val teiUid = enrollment.trackedEntityInstance() ?: return null
        val tei = d2.trackedEntityModule().trackedEntityInstances()
            .byUid().eq(teiUid)
            .withTrackedEntityAttributeValues()
            .one()
            .blockingGet()
            ?: return null
        val identity = transformations.transform(tei, program, enrollment).learnerIdentity()
        val originOrgUnit = event.organisationUnit().orEmpty()
        val destinationOrgUnit = event.dataValue(transfer.destinySchool).orEmpty()

        return TeiTransfer(
            eventUid = event.uid(),
            teiUid = teiUid,
            enrollmentUid = enrollmentUid,
            recordName = identity.name,
            firstAttributeValue = identity.firstAttributeValue,
            originOrgUnit = originOrgUnit,
            originSchoolName = orgUnitName(originOrgUnit),
            destinationOrgUnit = destinationOrgUnit,
            destinationSchoolName = orgUnitName(destinationOrgUnit),
            grade = learnerGrade(enrollmentUid, config),
            reason = transferReason(event, transfer),
            status = transfer.transferStatusOf(event.dataValue(transfer.status)),
            requestedAt = event.created() ?: event.eventDate() ?: Date(),
        )
    }

    /**
     * The class the learner is in, read from the registration event the same way the
     * listings read it. Blank when the configuration does not name the data element.
     */
    private fun learnerGrade(enrollmentUid: String, config: SEMISConfigItem): String {
        val gradeDataElement = config.registration?.grade?.takeIf { it.isNotBlank() }
            ?: return ""
        val registrationStage = config.registration?.programStage?.takeIf { it.isNotBlank() }
            ?: return ""

        return d2.eventModule().events()
            .byEnrollmentUid().eq(enrollmentUid)
            .byProgramStageUid().eq(registrationStage)
            .byDeleted().isFalse
            .withTrackedEntityDataValues()
            .blockingGet()
            .firstNotNullOfOrNull { event -> event.dataValue(gradeDataElement) }
            .orEmpty()
    }

    /**
     * Why the learner is being sent. The configuration names the destination and the
     * status but not the reason, so it is whatever else the request carries.
     */
    private fun transferReason(event: Event, transfer: Transfer): String =
        event.trackedEntityDataValues()
            ?.filterNot { it.dataElement() == transfer.destinySchool }
            ?.filterNot { it.dataElement() == transfer.status }
            ?.firstNotNullOfOrNull { it.value()?.takeIf(String::isNotBlank) }
            .orEmpty()

    private fun orgUnitName(orgUnit: String): String =
        orgUnit.takeIf { it.isNotBlank() }
            ?.let {
                d2.organisationUnitModule().organisationUnits()
                    .uid(it)
                    .blockingGet()
                    ?.displayName()
            }
            .orEmpty()

    private fun Event.dataValue(dataElement: String?): String? =
        trackedEntityDataValues()
            ?.find { it.dataElement() == dataElement }
            ?.value()

    private fun Transfer?.requireConfigured(): Transfer {
        val transfer = this
            ?: error(resourceManager.getString(R.string.transfer_configuration_missing))
        require(transfer.enabled == true) {
            resourceManager.getString(R.string.transfer_not_enabled)
        }
        require(!transfer.programStage.isNullOrBlank()) {
            resourceManager.getString(R.string.transfer_program_stage_missing)
        }
        require(!transfer.destinySchool.isNullOrBlank()) {
            resourceManager.getString(R.string.transfer_destination_school_missing)
        }
        require(!transfer.status.isNullOrBlank()) {
            resourceManager.getString(R.string.transfer_status_missing)
        }
        require(!transfer.pendingStatusCode().isNullOrBlank()) {
            resourceManager.getString(R.string.transfer_pending_status_missing)
        }
        return transfer
    }
}
