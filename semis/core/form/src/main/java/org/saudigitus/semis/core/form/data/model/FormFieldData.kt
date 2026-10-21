package org.saudigitus.semis.core.form.data.model

import org.saudigitus.semis.core.data.model.OptionModel

/**
 * One value, belonging to one person.
 *
 * The form holds a single definition of its fields and a value of this shape per person, so
 * anything a rule says about a value has to be carried here: a mark above what the deployment
 * allows concerns the learner who has it, and stating it on the shared field would raise it on
 * everyone at once.
 */
data class FormFieldData(
    val tei: String,
    val event: String? = null,
    val dataElement: String,
    val value: String? = null,
    val optionModel: OptionModel? = null,
    val isUpdated: Boolean = false,
    val hasError: Boolean = false,
    val errorMessage: String? = null,
    val hasWarning: Boolean = false,
    val warningMessage: String? = null,
) {
    override fun toString(): String {
        return "{ tei: $tei, value: $value, isUpdated: $isUpdated }"
    }
}