package org.saudigitus.campaign.core.form.ui.state

import org.saudigitus.campaign.core.data.models.OrgUnit
import org.saudigitus.campaign.core.form.data.models.FormSectionModel

sealed class FormSectionUiState {
    object Idle : FormSectionUiState()
    object Loading : FormSectionUiState()

    data class HasFormSection(
        val formType: FormSectionType,
        val previousType: FormSectionType? = null,
        val orgUnit: OrgUnit? = null,
        val program: String? = null,
        val trackerId: String? = null,
        val enrollmentId: String? = null,
        val programStage: String? = null,
        val date: String? = null,
        val formSections: List<FormSectionModel>
    ) : FormSectionUiState()

}