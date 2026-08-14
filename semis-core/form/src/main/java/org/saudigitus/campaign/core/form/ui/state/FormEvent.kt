package org.saudigitus.campaign.core.form.ui.state

import org.saudigitus.campaign.core.data.models.OrgUnit
import org.saudigitus.campaign.core.form.data.models.FormSectionModel

sealed interface FormEvent {
    data object LoadForm : FormEvent

    data class SelectedOU(val orgUnit: OrgUnit) : FormEvent
    data class SelectedDate(val date: String) : FormEvent

    data class UpdateField(
        val section: FormSectionModel,
        val uid: String,
        val value: String
    ) : FormEvent

    data class SearchFieldQuery(
        val section: FormSectionModel,
        val uid: String,
        val query: String
    ) : FormEvent

    data object CancelSave : FormEvent
    data object ConfirmSave: FormEvent

    data object SaveEvent : FormEvent

    data class ShowError(val message: String) : FormEvent
    data class ShowSuccess(val message: String) : FormEvent
    data object NavigateBack : FormEvent
}