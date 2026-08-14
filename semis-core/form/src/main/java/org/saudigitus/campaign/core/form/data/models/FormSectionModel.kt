package org.saudigitus.campaign.core.form.data.models

data class FormSectionModel(
    val uid: String,
    val programStage: String? = null,
    val eventUid: String? = null,
    val code: String?,
    val name: String?,
    val description: String? = null,
    val formFields: List<FormFieldModel> = emptyList(),
    val sortOrder: Int? = -1,
    val rendered: Boolean = true,
    val registrationDate: Long? = null,
) {
    fun countMandatoryFields(): Int {
        return formFields.count { it.mandatory == true }
    }

    fun countUnfilledMandatoryFields(): Int {
        return formFields.count { it.mandatory == true && it.value.isNullOrEmpty() }
    }

    fun hasUnfilledMandatoryFields(): Boolean {
        return formFields.any { it.mandatory == true && it.value.isNullOrEmpty() && it.rendered == true }
    }

    fun countFields(): Int {
        return formFields.count()
    }

    override fun toString(): String {
        return "name: ${name.orEmpty()}, render: $rendered"
    }
}
