package org.saudigitus.campaign.core.form.rules

import org.saudigitus.campaign.core.form.data.models.FormSectionModel

/** Fallback for applications without Campaign rule-engine modules. */
class FormValidationRulesHelper : FormValidationRulesRepository {
    override suspend fun applyValidations(
        program: String,
        programStage: String?,
        event: String?,
        enrollment: String?,
        formSections: Map<String, FormSectionModel>,
    ): MutableMap<String, FormSectionModel> = formSections.toMutableMap()
}
