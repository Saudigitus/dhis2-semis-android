package org.saudigitus.campaign.core.form.ui.model

import org.saudigitus.campaign.core.form.ui.state.FormSectionType

/**
 * Represents a request to initialize a dynamic form section.
 *
 * This sealed class defines the different entry points for the form engine.
 * Each request type describes the context required to load a form and
 * retrieve the corresponding form sections from the repository.
 *
 * The form engine supports two primary flows:
 *
 * 1. Enrollment forms
 * 2. Event forms
 *
 * These requests are typically triggered by navigation actions in the UI.
 */
sealed class FormSection(
    open val orgUnit: String,
    open val program: String,
) {
    abstract val formType: FormSectionType

    /**
     * Request used to create a new enrollment in a tracker program.
     *
     * This request initializes a form for collecting the tracked entity
     * attributes required to enroll a new tracked entity instance (TEI)
     * into a program.
     *
     * Typical flow:
     *
     * 1. User selects a program
     * 2. User selects an organisation unit
     * 3. Enrollment form is loaded
     * 4. User submits tracked entity attributes
     * 5. Enrollment is created
     *
     * Required parameters:
     *
     * @param orgUnit Organisation unit where the enrollment will be created
     * @param program Tracker program UID
     */
    data class NewEnrollment(
        override val orgUnit: String,
        override val program: String
    ): FormSection(orgUnit, program) {
        override val formType = FormSectionType.NEW_ENROLLMENT
    }

    data class EditEnrollment(
        override val orgUnit: String,
        override val program: String,
        val enrollment: String,
        val tei: String,
    ): FormSection(orgUnit, program) {
        override val formType = FormSectionType.EDIT_ENROLLMENT
    }

    /**
     * Request used to create a new event form.
     *
     * This request supports two different event creation contexts:
     *
     * 1. **Program without registration**
     *    - Events are created independently
     *    - No tracked entity instance is required
     *
     * 2. **Program with registration**
     *    - Events are linked to an existing enrollment
     *    - Requires a tracked entity instance and enrollment
     *
     * The form engine determines the event type automatically based
     * on the provided parameters.
     *
     * Event Types
     * ----------
     *
     * **Event Without Registration**
     *
     * Used in programs that do not require tracked entity enrollment.
     *
     * Required:
     * - orgUnit
     * - program
     *
     * Optional:
     * - programStage
     *
     * Example:
     *
     * ```
     * FormSection.NewEvent(
     *     orgUnit = "OU123",
     *     program = "PROGRAM123"
     * )
     * ```
     *
     * **Event With Registration**
     *
     * Used when creating an event for an existing enrollment
     * in a tracker program.
     *
     * Required:
     * - orgUnit
     * - program
     * - trackerUid
     * - enrollment
     * - programStage
     *
     * Example:
     *
     * ```
     * FormSection.NewEvent(
     *     orgUnit = "OU123",
     *     program = "PROGRAM123",
     *     programStage = "STAGE123",
     *     trackerUid = "TEI123",
     *     enrollment = "ENROLL123"
     * )
     * ```
     *
     * Parameters:
     *
     * @param orgUnit Organisation unit where the event will be created
     * @param program Program UID
     * @param programStage Optional program stage UID
     * @param trackerUid Tracked entity instance UID (required for tracker events)
     * @param enrollment Enrollment UID (required for tracker events)
     */
    data class NewEvent(
        override val orgUnit: String,
        override val program: String,
        val trackerUid: String? = null,
        val enrollment: String? = null,
        val programStage: String? = null
    ): FormSection(orgUnit, program) {
        override val formType =
            if (enrollment == null)
                FormSectionType.NEW_EVENT_WITHOUT_REGISTRATION
            else
                FormSectionType.NEW_EVENT_WITH_REGISTRATION
    }
}