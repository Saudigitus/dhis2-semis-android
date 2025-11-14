package org.saudigitus.semis.core.form.ui.state

sealed interface FormEvent {
    data object LoadForm : FormEvent
    data class UpdateField(val dataElementUid: String, val value: String) : FormEvent
    data object SaveEvent : FormEvent

    data class ShowError(val message: String) : FormEvent
    data class ShowSuccess(val message: String) : FormEvent
    data object NavigateBack : FormEvent
}