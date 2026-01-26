package org.saudigitus.semis.core.form.data.model

import org.saudigitus.semis.core.data.model.OptionModel

data class FormFieldData(
    val tei: String,
    val event: String? = null,
    val dataElement: String,
    val value: String? = null,
    val optionModel: OptionModel? = null,
    val isUpdated: Boolean = false
) {
    override fun toString(): String {
        return "{ tei: $tei, value: $value, isUpdated: $isUpdated }"
    }
}