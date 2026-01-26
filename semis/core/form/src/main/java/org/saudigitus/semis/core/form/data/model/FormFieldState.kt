package org.saudigitus.semis.core.form.data.model

import org.hisp.dhis.android.core.common.ValueType
import org.saudigitus.semis.core.data.model.OptionModel

data class FormFieldState(
    val eventUid: String? = null,
    val dataElementUid: String,
    val label: String,
    val valueType: ValueType,
    val value: String? = null,
    val optionSet: List<OptionModel>? = null,
    val mandatory: Boolean = false,
    val rendered: Boolean = true,
    val enabled: Boolean = true,
    val isAttendanceType: Boolean = false,
    val hasError: Boolean = false,
    val errorMessage: String? = null
) {
    override fun toString(): String {
        return "FormFieldState(eventUid=$eventUid, dataElementUid='$dataElementUid', label='$label', valueType=$valueType, value=$value, optionSet=$optionSet, mandatory=$mandatory, rendered=$rendered, enabled=$enabled, isAttendanceType=$isAttendanceType, hasError=$hasError, errorMessage=$errorMessage)"
    }
}