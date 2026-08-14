package org.saudigitus.campaign.core.form.rules

import org.saudigitus.campaign.core.form.data.models.FormSectionModel

interface FormValidationRulesRepository {
    suspend fun applyValidations(
        program: String,
        programStage: String? = null,
        event: String? = null,
        enrollment: String? = null,
        formSections: Map<String, FormSectionModel>,
    ): MutableMap<String, FormSectionModel>
}
