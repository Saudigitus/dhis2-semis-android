package org.saudigitus.campaign.core.form.rules

import com.formrules.engine.EventData
import com.formrules.engine.FormEvaluation
import org.saudigitus.campaign.core.form.data.models.FormSectionModel

interface FormValidationRulesRepository {
    suspend fun evaluateEnrollment(program: String, tei: String): FormEvaluation?
    suspend fun evaluateEvent(
        program: String,
        stage: String,
        tei: String,
        event: String? = null,
        currentEvent: EventData? = null,
    ): FormEvaluation?

    suspend fun applyValidations(
        program: String,
        programStage: String? = null,
        event: String? = null,
        enrollment: String? = null,
        formSections:  Map<String, FormSectionModel>,
    ): MutableMap<String, FormSectionModel>
}