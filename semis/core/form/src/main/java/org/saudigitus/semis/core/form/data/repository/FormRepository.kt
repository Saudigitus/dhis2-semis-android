package org.saudigitus.semis.core.form.data.repository

import org.saudigitus.semis.core.form.data.model.FormFieldState

interface FormRepository: AttendanceEventRepository {

    fun allowFormEdition(enabled: Boolean)

    suspend fun getFormFields(
        program: String,
        stage: String,
        dl: String? = null
    ): List<FormFieldState>

    suspend fun applyProgramRules(
        orgUnit: String,
        program: String,
        programStage: String,
        fields: List<FormFieldState>,
    ): List<FormFieldState>

    fun reset()
}