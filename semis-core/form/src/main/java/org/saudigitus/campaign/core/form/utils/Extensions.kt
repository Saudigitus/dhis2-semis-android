package org.saudigitus.campaign.core.form.utils

import org.saudigitus.campaign.core.data.models.FormFieldEntity
import org.saudigitus.campaign.core.data.models.FormSectionEntity
import org.saudigitus.campaign.core.form.data.models.FormFieldModel
import org.saudigitus.campaign.core.form.data.models.FormSectionModel
import org.saudigitus.campaign.core.form.ui.model.FormSection
import org.saudigitus.campaign.core.navigation.AppRoute
import org.saudigitus.campaign.core.navigation.FormType

fun List<FormSectionModel>.completionPercentage(): Float {
    val trackableFields = filter { it.rendered }
        .flatMap { it.formFields }
        .filter { it.rendered == true }

    if (trackableFields.isEmpty()) return 1f

    val mandatoryFields = trackableFields.filter { it.mandatory == true }

    val baseFields = mandatoryFields.ifEmpty {
        trackableFields
    }

    val filled = baseFields.count { !it.value.isNullOrEmpty() }

    return filled.toFloat() / baseFields.size
}

fun FormSectionModel.completionPercentage(): Float {
    val mandatoryFields = formFields
        .filter { it.mandatory == true && it.rendered == true && it.enabled == true }

    if (mandatoryFields.isEmpty()) return 1f

    val filled = mandatoryFields.count { !it.value.isNullOrEmpty() }
    return filled.toFloat() / mandatoryFields.size
}

fun List<FormSectionModel>.checkUnfilledMandatoryFields(): Boolean {
    return this.any { it.rendered && it.hasUnfilledMandatoryFields() }
}

private fun FormFieldModel.isBlockingField(): Boolean {
    return rendered == true &&
        (
            hasError == true ||
                (mandatory == true && enabled == true && value.isNullOrEmpty())
            )
}

fun FormSectionModel.firstBlockingFieldIndex(): Int {
    return formFields.indexOfFirst {
        it.isBlockingField()
    }
}

fun List<FormSectionModel>.hasBlockingFields(): Boolean {
    return any { section -> section.rendered && section.firstBlockingFieldIndex() != -1 }
}

/**
 * Returns the index of the first mandatory field that has no value.
 * If there are no mandatory fields, returns -1.
 */
fun List<FormSectionModel>.getFirstMandatoryFieldIndex(): Int {
    val mandatoryFields = this
        .filter { it.rendered }
        .flatMap { it.formFields }
        .filter { it.mandatory == true && it.rendered == true && it.enabled == true }

    return if (mandatoryFields.isEmpty()) -1 else mandatoryFields.indexOfFirst {
        it.value.isNullOrEmpty()
    }
}

fun List<FormFieldModel>.toEntities(): List<FormFieldEntity> {
    return filter { it.hasError != true }
        .map {
            FormFieldEntity(
                uid = it.uid,
                value = it.value,
            )
        }
}

fun List<FormSectionModel>.toSectionEntities(): List<FormSectionEntity> {
    return filter { it.rendered }.map {
        FormSectionEntity(
            uid = it.uid,
            programStage = it.programStage,
            eventUid = it.eventUid,
            code = it.code,
            name = it.name,
            description = it.description,
            formFields = it.formFields.filter { field -> field.rendered == true }.toEntities(),
            sortOrder = it.sortOrder,
        )
    }
}



fun AppRoute.FormRoute.toFormSection(): FormSection? {
    return when (this.formType) {
        FormType.NEW_ENROLLMENT -> {
            FormSection.NewEnrollment(
                orgUnit = this.orgUnitUid.orEmpty(),
                program = this.programUid.orEmpty(),
            )
        }
        FormType.EDIT_ENROLLMENT -> {
            FormSection.EditEnrollment(
                orgUnit = this.orgUnitUid.orEmpty(),
                program = this.programUid.orEmpty(),
                enrollment = this.enrollmentUid.orEmpty(),
                tei = this.trackedEntityUid.orEmpty(),
            )
        }
        FormType.NEW_EVENT_WITH_REGISTRATION, FormType.NEW_EVENT_WITHOUT_REGISTRATION -> {
            FormSection.NewEvent(
                orgUnit = this.orgUnitUid.orEmpty(),
                program = this.programUid.orEmpty(),
                trackerUid = this.trackedEntityUid,
                enrollment = this.enrollmentUid,
                programStage = this.programStageUid,
            )
        }

        else -> null
    }
}
