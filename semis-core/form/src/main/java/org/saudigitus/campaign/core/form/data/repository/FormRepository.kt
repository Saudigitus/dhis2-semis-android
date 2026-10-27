package org.saudigitus.campaign.core.form.data.repository

import org.saudigitus.campaign.core.data.models.OrgUnit
import org.saudigitus.campaign.core.data.models.OuHideStrategy
import org.saudigitus.campaign.core.form.data.models.FormResult
import org.saudigitus.campaign.core.form.data.models.FormSectionModel
import org.saudigitus.campaign.core.form.ui.state.FormSectionType

interface FormRepository {
    /**
     * Persists form data for either a new Enrollment or a new Event,
     * depending on the provided [FormSectionType].
     *
     * This function is responsible for:
     * - Creating a new Enrollment and its associated Tracked Entity Instance (TEI), OR
     * - Creating a new Event
     * - Mapping and persisting all form section data and field values
     * - Assigning the record to the specified organisation unit and program
     * - Setting the provided date as enrollment or event date
     *
     * Behavior depends on [formType]:
     * - If the type represents an Enrollment:
     *      - A new TEI is created
     *      - The TEI is enrolled in the specified program
     *      - [FormResult.enrollment] and [FormResult.tei] will be populated
     * - If the type represents an Event:
     *      - A new Event is created
     *      - [FormResult.isEventSaved] will be true if the operation succeeds
     *
     * @param formType Defines whether the form corresponds to an Enrollment or an Event.
     * @param orgUnit The organisation unit UID where the data will be saved.
     * @param program The program UID associated with the enrollment or event.
     * @param date The enrollment or event date in ISO-8601 format (e.g., "yyyy-MM-dd").
     * @param formSections A list of [FormSectionModel] containing all sections
     *                     and their respective form field values to be persisted.
     *
     * @return [FormResult] containing:
     * - enrollment: The Enrollment UID (if created), otherwise null
     * - tei: The Tracked Entity Instance UID (if created), otherwise null
     * - isEventSaved: True if an Event was successfully created
     *
     * @throws Exception If the enrollment or event creation fails.
     */
    suspend fun save(
        formType: FormSectionType,
        orgUnit: String,
        program: String,
        date: String,
        tei: String? = null,
        enrollment: String? = null,
        formSections: List<FormSectionModel>
    ): FormResult

    suspend fun getFormSections(
        orgUnit: String,
        program: String,
        tei: String? = null,
        enrollment: String? = null,
        vararg programStages: String?
    ): List<FormSectionModel>

    suspend fun getFormSections(
        orgUnit: String,
        program: String,
        tei: String? = null,
    ): List<FormSectionModel>

    /**
     * Writes a whole enrollment in one go, from the values gathered across every step.
     *
     * Persisting each step as it is completed leaves a learner behind whenever the user stops or
     * something fails partway, and that half made record is later synced. Committing once, at the
     * end, means an interrupted enrollment simply never existed.
     *
     * If any part of the write fails, whatever was already created is removed before the failure is
     * reported, so the device is never left holding a learner without the rest of the enrollment.
     *
     * @param attributes the sections captured for the tracked entity attributes.
     * @param stages the sections captured for each program stage the user filled in, keyed by stage.
     * @param backgroundStages stages that only need an empty event, created without asking the user.
     */
    suspend fun saveEnrollment(
        orgUnit: String,
        program: String,
        date: String,
        attributes: List<FormSectionModel>,
        stages: Map<String, List<FormSectionModel>>,
        backgroundStages: List<String>,
    ): FormResult

    suspend fun applyProgramRules(
        orgUnit: String,
        program: String,
        programStage: String? = null,
        event: String? = null,
        enrollment: String? = null,
        formSections: List<FormSectionModel>,
    ): List<FormSectionModel>

    suspend fun searchOrgUnits(
        query: String? = null,
        orgUnit: String,
        ouHideStrategy: OuHideStrategy? = null
    ): List<OrgUnit>

    suspend fun getDefaultProgramStage(program: String): String?

    suspend fun programStagesToHide(): List<String>

    suspend fun getOrgUnitName(ou: String): String?

    suspend fun deleteEvent(event: String)
}