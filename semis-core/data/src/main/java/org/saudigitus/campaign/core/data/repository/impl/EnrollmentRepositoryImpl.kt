package org.saudigitus.campaign.core.data.repository.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.dhis2.commons.resources.ResourceManager
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.enrollment.EnrollmentCreateProjection
import org.saudigitus.campaign.core.data.R
import org.saudigitus.campaign.core.data.models.FormFieldEntity
import org.saudigitus.campaign.core.data.repository.EnrollmentRepository
import org.saudigitus.campaign.core.data.repository.TeiRepository

class EnrollmentRepositoryImpl(
    private val d2: D2,
    private val teiRepository: TeiRepository,
    private val resourceManager: ResourceManager
) : EnrollmentRepository {

    private suspend fun createProjection(
        orgUnit: String,
        program: String,
        fields: List<FormFieldEntity>,
    ): Pair<EnrollmentCreateProjection?, String?> {
        val tracker = teiRepository.create(orgUnit, program, fields)

        val projection = EnrollmentCreateProjection.builder()
            .organisationUnit(orgUnit)
            .program(program)
            .trackedEntityInstance(tracker)
            .build()

        return Pair(projection, tracker)
    }

    override suspend fun create(
        orgUnit: String,
        program: String,
        date: String,
        fields: List<FormFieldEntity>
    ) =
        withContext(Dispatchers.IO) {
            val projection = createProjection(orgUnit, program, fields)

            if (projection.first == null || projection.second == null) {
                throw IllegalStateException(resourceManager.getString(R.string.error_creating_enrollment))
            }

            val uid = d2.enrollmentModule().enrollments().blockingAdd(projection.first!!)

            val repository = d2.enrollmentModule().enrollments().uid(uid)
            repository.setEnrollmentDate(java.sql.Date.valueOf(date))
            repository.setIncidentDate(java.sql.Date.valueOf(date))

            Pair(uid, projection.second!!)
        }

    override suspend fun update(
        enrollment: String,
        tei: String,
        date: String,
        fields: List<FormFieldEntity>
    ) = withContext(Dispatchers.IO)  {

        teiRepository.update(
            tei,
            fields
        )

        Pair(tei, enrollment)
    }

    override suspend fun getEnrollmentDate(tei: String?) = withContext(Dispatchers.IO) {
        if (tei.isNullOrEmpty()) return@withContext null

        d2.enrollmentModule()
            .enrollments()
            .byTrackedEntityInstance().eq(tei)
            .blockingGet().lastOrNull()
            ?.enrollmentDate()
            ?.time
    }
}