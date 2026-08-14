package org.saudigitus.campaign.core.form.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.hisp.dhis.android.core.D2
import org.saudigitus.campaign.core.data.models.OptionModel
import org.saudigitus.campaign.core.data.models.OrgUnit
import org.saudigitus.campaign.core.data.models.OuHideStrategy
import org.saudigitus.campaign.core.data.repository.EnrollmentRepository
import org.saudigitus.campaign.core.data.repository.OptionRepository
import org.saudigitus.campaign.core.data.repository.ProgramRepository
import org.saudigitus.campaign.core.form.data.models.FormFieldModel
import org.saudigitus.campaign.core.form.data.models.FormResult
import org.saudigitus.campaign.core.form.data.models.FormSectionModel
import org.saudigitus.campaign.core.form.ui.state.FormSectionType
import org.saudigitus.campaign.core.form.utils.toEntities
import javax.inject.Inject

/**
 * SEMIS adapter for the original Campaign form UI.  It retains the Campaign
 * form sections and field components while using only the tracker enrollment
 * data needed by SEMIS.
 */
class SemisEnrollmentFormRepository @Inject constructor(
    private val d2: D2,
    private val optionRepository: OptionRepository,
    private val programRepository: ProgramRepository,
    private val enrollmentRepository: EnrollmentRepository,
) : FormRepository {
    override suspend fun save(
        formType: FormSectionType,
        orgUnit: String,
        program: String,
        date: String,
        tei: String?,
        enrollment: String?,
        formSections: List<FormSectionModel>,
    ): FormResult = withContext(Dispatchers.IO) {
        val fields = formSections
            .filter { it.rendered }
            .flatMap { it.formFields }
            .filter { it.rendered == true }
            .toEntities()

        when (formType) {
            FormSectionType.NEW_ENROLLMENT -> {
                val (enrollmentUid, teiUid) = enrollmentRepository.create(orgUnit, program, date, fields)
                FormResult(enrollment = enrollmentUid, tei = teiUid)
            }

            FormSectionType.EDIT_ENROLLMENT -> {
                val (teiUid, enrollmentUid) = enrollmentRepository.update(
                    enrollment = enrollment.orEmpty(),
                    tei = tei.orEmpty(),
                    date = date,
                    fields = fields,
                )
                FormResult(enrollment = enrollmentUid, tei = teiUid)
            }

            else -> FormResult(isEventSaved = false)
        }
    }

    override suspend fun getFormSections(
        orgUnit: String,
        program: String,
        tei: String?,
        enrollment: String?,
        vararg programStages: String?,
    ): List<FormSectionModel> = emptyList()

    override suspend fun getFormSections(
        orgUnit: String,
        program: String,
        tei: String?,
    ): List<FormSectionModel> = withContext(Dispatchers.IO) {
        programRepository.getTrackedEntityAttributeWithSection(program).map { section ->
            FormSectionModel(
                uid = section.uid,
                code = section.code,
                name = section.displayName,
                description = section.description,
                sortOrder = section.sortOrder,
                formFields = section.attributes.map { attribute ->
                    val optionSetUid = attribute.optionSetUid
                    FormFieldModel(
                        uid = attribute.uid,
                        label = attribute.displayFormName.orEmpty(),
                        valueType = attribute.valueType,
                        mandatory = attribute.mandatory,
                        baseMandatory = attribute.mandatory,
                        optionSet = if (optionSetUid != null) {
                            optionRepository.getOptions(optionSetUid).map {
                                OptionModel(
                                    uid = it.uid(),
                                    code = it.code(),
                                    displayName = it.displayName(),
                                    sortOrder = it.sortOrder(),
                                )
                            }
                        } else emptyList(),
                    )
                },
            )
        }
    }

    override suspend fun applyProgramRules(
        orgUnit: String,
        program: String,
        programStage: String?,
        event: String?,
        enrollment: String?,
        formSections: List<FormSectionModel>,
    ): List<FormSectionModel> = formSections.map { section ->
        section.copy(
            rendered = true,
            formFields = section.formFields.map { field ->
                field.copy(
                    rendered = true,
                    mandatory = field.baseMandatory,
                    hasError = field.hasError,
                    errorMessage = field.errorMessage,
                    hasWarning = false,
                    warningMessage = null,
                )
            },
        )
    }

    override suspend fun searchOrgUnits(
        query: String?,
        orgUnit: String,
        ouHideStrategy: OuHideStrategy?,
    ): List<OrgUnit> = emptyList()

    override suspend fun getDefaultProgramStage(program: String): String? = withContext(Dispatchers.IO) {
        d2.programModule().programStages()
            .byProgramUid().eq(program)
            .one()
            .blockingGet()
            ?.uid()
    }

    override suspend fun programStagesToHide(): List<String> = emptyList()

    override suspend fun getOrgUnitName(ou: String): String? = withContext(Dispatchers.IO) {
        d2.organisationUnitModule().organisationUnits()
            .uid(ou)
            .blockingGet()
            ?.displayName()
    }

    override suspend fun deleteEvent(event: String) = Unit
}
