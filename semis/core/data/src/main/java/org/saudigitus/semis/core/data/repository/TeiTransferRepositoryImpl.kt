package org.saudigitus.semis.core.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.dhis2.commons.resources.ResourceManager
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventStatus
import org.saudigitus.semis.core.data.R
import org.saudigitus.semis.core.data.model.app_config.Transfer
import org.saudigitus.semis.core.data.model.app_config.approvedStatusCode
import org.saudigitus.semis.core.data.model.app_config.isIncomingEnabledAndConfigured
import org.saudigitus.semis.core.data.model.app_config.pendingStatusCode
import org.saudigitus.semis.core.data.model.app_config.rejectedStatusCode
import org.saudigitus.semis.core.data.model.transfer.IncomingTeiTransfer
import org.saudigitus.semis.core.data.model.transfer.OutgoingTeiTransfer
import org.saudigitus.semis.core.data.model.transfer.TeiTransferFailure
import org.saudigitus.semis.core.data.model.transfer.TeiTransferLearner
import org.saudigitus.semis.core.data.model.transfer.TeiTransferMetadata
import org.saudigitus.semis.core.data.model.transfer.TeiTransferRequest
import org.saudigitus.semis.core.data.model.transfer.TeiTransferResult
import org.saudigitus.semis.core.data.model.transfer.TransferDecision
import org.saudigitus.semis.core.data.model.transfer.learnerIdentity
import org.saudigitus.semis.core.data.utils.Transformations
import org.saudigitus.semis.core.utils.DateHelper
import javax.inject.Inject

class TeiTransferRepositoryImpl @Inject constructor(
    private val d2: D2,
    private val appConfigRepository: AppConfigRepository,
    private val eventRepository: EventRepository,
    private val transformations: Transformations,
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
            originSchoolDataElement = transfer.originSchool.orEmpty(),
            destinationSchoolDataElement = transfer.destinySchool.orEmpty(),
            statusDataElement = transfer.status.orEmpty(),
            pendingStatusCode = transfer.pendingStatusCode().orEmpty(),
        )
    }

    override suspend fun transfer(request: TeiTransferRequest): TeiTransferResult =
        withContext(Dispatchers.IO) {
            require(request.learners.isNotEmpty()) {
                resourceManager.getString(R.string.transfer_learner_required)
            }
            require(request.destinationOrgUnit.isNotBlank()) {
                resourceManager.getString(R.string.transfer_destination_required)
            }

            val transfer = appConfigRepository.getAppConfig(request.program)
                ?.transfer
                .requireConfigured()
            val transferred = mutableListOf<String>()
            val failures = mutableListOf<TeiTransferFailure>()

            request.learners.forEach { learner ->
                runCatching {
                    transferLearner(request, learner, transfer)
                }.onSuccess {
                    transferred += learner.teiUid
                }.onFailure { error ->
                    failures += TeiTransferFailure(
                        teiUid = learner.teiUid,
                        message = error.message
                            ?: resourceManager.getString(R.string.transfer_failed),
                    )
                }
            }

            TeiTransferResult(
                transferredTeiUids = transferred,
                failures = failures,
            )
        }

    override suspend fun getIncomingTransfers(
        program: String,
        currentOrgUnit: String,
    ): List<IncomingTeiTransfer> = withContext(Dispatchers.IO) {
        val transfer = appConfigRepository.getAppConfig(program)
            ?.transfer
            ?.takeIf { it.isIncomingEnabledAndConfigured() }
            ?: return@withContext emptyList()

        d2.eventModule().events()
            .byOrganisationUnitUid().eq(currentOrgUnit)
            .byProgramUid().eq(program)
            .byProgramStageUid().eq(transfer.programStage.orEmpty())
            .byDeleted().isFalse
            .withTrackedEntityDataValues()
            .blockingGet()
            .asSequence()
            .filter { event ->
                event.status() == EventStatus.ACTIVE &&
                    event.dataValue(transfer.status) == transfer.pendingStatusCode() &&
                    event.dataValue(transfer.destinySchool) == currentOrgUnit
            }
            .mapNotNull { event -> incomingTransfer(event, program, transfer) }
            .sortedByDescending { it.effectiveDate }
            .toList()
    }

    override suspend fun getOutgoingTransfers(
        program: String,
        currentOrgUnit: String,
    ): List<OutgoingTeiTransfer> = withContext(Dispatchers.IO) {
        val transfer = appConfigRepository.getAppConfig(program)
            ?.transfer
            ?.takeIf { it.isIncomingEnabledAndConfigured() }
            ?: return@withContext emptyList()

        d2.eventModule().events()
            .byProgramUid().eq(program)
            .byProgramStageUid().eq(transfer.programStage.orEmpty())
            .byDeleted().isFalse
            .withTrackedEntityDataValues()
            .blockingGet()
            .asSequence()
            .filter { event -> event.dataValue(transfer.originSchool) == currentOrgUnit }
            .mapNotNull { event -> outgoingTransfer(event, program, transfer) }
            .sortedByDescending { it.effectiveDate }
            .toList()
    }

    override suspend fun decideIncomingTransfer(
        program: String,
        currentOrgUnit: String,
        eventUid: String,
        decision: TransferDecision,
    ) = withContext(Dispatchers.IO) {
        val transfer = appConfigRepository.getAppConfig(program)
            ?.transfer
            .requireConfigured()
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
        require(event.organisationUnit() == currentOrgUnit) {
            resourceManager.getString(R.string.transfer_wrong_school)
        }
        require(event.dataValue(transfer.destinySchool) == currentOrgUnit) {
            resourceManager.getString(R.string.transfer_not_destination)
        }
        require(event.status() == EventStatus.ACTIVE) {
            resourceManager.getString(R.string.transfer_not_pending)
        }

        val statusCode = when (decision) {
            TransferDecision.APPROVE -> transfer.approvedStatusCode()
                ?: error(resourceManager.getString(R.string.transfer_approved_status_missing))
            TransferDecision.REJECT -> transfer.rejectedStatusCode()
                ?: error(resourceManager.getString(R.string.transfer_rejected_status_missing))
        }

        if (decision == TransferDecision.REJECT) {
            returnLearnerToOrigin(event, program, transfer)
        }

        d2.trackedEntityModule().trackedEntityDataValues()
            .value(eventUid, transfer.status.orEmpty())
            .blockingSet(statusCode)

        eventRepository.setEventStatus(eventUid, EventStatus.COMPLETED)
    }

    private fun returnLearnerToOrigin(
        event: Event,
        program: String,
        transfer: Transfer,
    ) {
        val originOrgUnit = event.dataValue(transfer.originSchool)
            ?.takeIf { it.isNotBlank() }
            ?: error(resourceManager.getString(R.string.transfer_origin_school_unknown))
        val enrollmentUid = event.enrollment()
            ?: error(resourceManager.getString(R.string.transfer_request_not_found))
        val enrollment = d2.enrollmentModule().enrollments()
            .uid(enrollmentUid)
            .blockingGet()
            ?: error(
                resourceManager.getString(
                    R.string.transfer_enrollment_not_found,
                    enrollmentUid,
                )
            )
        val teiUid = enrollment.trackedEntityInstance()
            ?: error(resourceManager.getString(R.string.transfer_request_not_found))

        d2.enrollmentModule().enrollments()
            .uid(enrollmentUid)
            .setOrganisationUnitUid(originOrgUnit)
        d2.trackedEntityModule().ownershipManager()
            .blockingTransfer(teiUid, program, originOrgUnit)
    }

    private suspend fun transferLearner(
        request: TeiTransferRequest,
        learner: TeiTransferLearner,
        transfer: Transfer,
    ) {
        val enrollmentRepository = d2.enrollmentModule().enrollments().uid(learner.enrollmentUid)
        val enrollment = enrollmentRepository.blockingGet()
            ?: error(
                resourceManager.getString(
                    R.string.transfer_enrollment_not_found,
                    learner.enrollmentUid,
                )
            )
        val sourceOrgUnit = enrollment.organisationUnit()
            ?: error(resourceManager.getString(R.string.transfer_enrollment_school_missing))
        require(sourceOrgUnit != request.destinationOrgUnit) {
            resourceManager.getString(R.string.transfer_same_school)
        }

        var transferEventUid: String? = null
        try {
            transferEventUid = eventRepository.createEvent(
                orgUnit = request.destinationOrgUnit,
                program = request.program,
                programStage = transfer.programStage.orEmpty(),
                enrollmentUid = learner.enrollmentUid,
                data = listOf(
                    transfer.originSchool.orEmpty() to sourceOrgUnit,
                    transfer.destinySchool.orEmpty() to request.destinationOrgUnit,
                    transfer.status.orEmpty() to transfer.pendingStatusCode(),
                ),
                eventDate = DateHelper.formatDate(request.effectiveDate.time),
                status = EventStatus.ACTIVE,
            )
            enrollmentRepository.setOrganisationUnitUid(request.destinationOrgUnit)
            d2.trackedEntityModule().ownershipManager().blockingTransfer(
                learner.teiUid,
                request.program,
                request.destinationOrgUnit,
            )
        } catch (error: Exception) {
            runCatching { enrollmentRepository.setOrganisationUnitUid(sourceOrgUnit) }
            runCatching {
                d2.trackedEntityModule().ownershipManager().blockingTransfer(
                    learner.teiUid,
                    request.program,
                    sourceOrgUnit,
                )
            }
            transferEventUid?.let { uid ->
                runCatching { eventRepository.deleteEvent(uid) }
            }
            throw error
        }
    }

    private fun incomingTransfer(
        event: Event,
        program: String,
        transfer: Transfer,
    ): IncomingTeiTransfer? {
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
        val originOrgUnit = event.dataValue(transfer.originSchool).orEmpty()
        val originSchoolName = d2.organisationUnitModule().organisationUnits()
            .uid(originOrgUnit)
            .blockingGet()
            ?.displayName()
            .orEmpty()

        return IncomingTeiTransfer(
            eventUid = event.uid(),
            teiUid = teiUid,
            enrollmentUid = enrollmentUid,
            learnerName = identity.name,
            firstAttributeValue = identity.firstAttributeValue,
            originOrgUnit = originOrgUnit,
            originSchoolName = originSchoolName,
            destinationOrgUnit = event.dataValue(transfer.destinySchool).orEmpty(),
            effectiveDate = event.eventDate() ?: java.util.Date(),
        )
    }

    private fun outgoingTransfer(
        event: Event,
        program: String,
        transfer: Transfer,
    ): OutgoingTeiTransfer? {
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
        val destinationOrgUnit = event.dataValue(transfer.destinySchool).orEmpty()
        val destinationSchoolName = d2.organisationUnitModule().organisationUnits()
            .uid(destinationOrgUnit)
            .blockingGet()
            ?.displayName()
            .orEmpty()

        val statusCode = event.dataValue(transfer.status).orEmpty()

        return OutgoingTeiTransfer(
            eventUid = event.uid(),
            teiUid = teiUid,
            enrollmentUid = enrollmentUid,
            learnerName = identity.name,
            firstAttributeValue = identity.firstAttributeValue,
            destinationOrgUnit = destinationOrgUnit,
            destinationSchoolName = destinationSchoolName,
            statusCode = statusCode,
            isPending = event.status() == EventStatus.ACTIVE &&
                statusCode == transfer.pendingStatusCode(),
            effectiveDate = event.eventDate() ?: java.util.Date(),
        )
    }

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
        require(!transfer.originSchool.isNullOrBlank()) {
            resourceManager.getString(R.string.transfer_origin_school_missing)
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
