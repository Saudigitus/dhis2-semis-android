package org.saudigitus.semis.core.form.data.repository

import org.saudigitus.semis.core.designsystem.components.bottomsheet.model.BottomSheetModel
import org.saudigitus.semis.core.form.data.model.FormFieldData
import org.saudigitus.semis.core.form.data.model.FormFieldState

interface FormRepository: AttendanceEventRepository {

    fun allowFormEdition(enabled: Boolean)

    suspend fun getFormFields(
        program: String,
        stage: String,
        dl: String? = null
    ): List<FormFieldState>

    /**
     * Applies the program rules to the values captured for each person.
     *
     * Evaluated one person at a time, from the values that person holds, because a rule about a
     * mark concerns whoever has that mark and the answer belongs on their value rather than on
     * the field every person shares.
     */
    suspend fun applyProgramRulesToRecords(
        orgUnit: String,
        program: String,
        programStage: String,
        fieldsData: List<FormFieldData>,
    ): List<FormFieldData>

    suspend fun applyProgramRules(
        orgUnit: String,
        program: String,
        programStage: String,
        fields: List<FormFieldState>,
    ): List<FormFieldState>

    fun individualFormSummary(
        formFieldsData: List<FormFieldData>
    ) : List<BottomSheetModel>

    fun reset()
}