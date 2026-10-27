package org.saudigitus.campaign.core.form.utils

import androidx.annotation.StringRes
import org.hisp.dhis.android.core.arch.helpers.Result
import org.hisp.dhis.android.core.common.ValueType
import org.saudigitus.campaign.core.form.R
import org.saudigitus.campaign.core.form.data.models.FormFieldModel

/**
 * Whether the value of this field can be stored as its value type, judged by the SDK's own
 * validator, the same one that refuses the value when it is written.
 *
 * A blank value is never wrong here, since whether one is needed is the business of the mandatory
 * check, and a field without a type is not judged.
 */
fun FormFieldModel.hasValueOfItsType(): Boolean {
    val type = valueType ?: return true
    val current = value?.takeIf { it.isNotBlank() } ?: return true

    return type.validator.validate(current) is Result.Success
}

/**
 * This field with the value type judged: flagged with what its value must look like when the type
 * does not allow it, and cleared of that flag once it does.
 *
 * The flag takes the place of a program rule error, which can only be read once the value means
 * something. Any other error is left alone, and the next evaluation of the rules restores one this
 * clears.
 */
fun FormFieldModel.withValueTypeChecked(message: (Int) -> String): FormFieldModel {
    val type = valueType ?: return this
    val typeMessage = message(type.invalidValueMessage())

    return when {
        !hasValueOfItsType() -> copy(hasError = true, errorMessage = typeMessage)
        hasError == true && errorMessage == typeMessage -> copy(hasError = false, errorMessage = null)
        else -> this
    }
}

/** What a value of this type must look like, said to the person typing it. */
@StringRes
fun ValueType.invalidValueMessage(): Int = when (this) {
    ValueType.INTEGER -> R.string.value_invalid_integer
    ValueType.INTEGER_POSITIVE -> R.string.value_invalid_integer_positive
    ValueType.INTEGER_NEGATIVE -> R.string.value_invalid_integer_negative
    ValueType.INTEGER_ZERO_OR_POSITIVE -> R.string.value_invalid_integer_zero_or_positive
    ValueType.NUMBER -> R.string.value_invalid_number
    ValueType.PERCENTAGE -> R.string.value_invalid_percentage
    ValueType.UNIT_INTERVAL -> R.string.value_invalid_unit_interval
    ValueType.PHONE_NUMBER -> R.string.value_invalid_phone_number
    ValueType.EMAIL -> R.string.value_invalid_email
    ValueType.LETTER -> R.string.value_invalid_letter
    ValueType.DATE, ValueType.AGE -> R.string.value_invalid_date
    ValueType.COORDINATE -> R.string.value_invalid_coordinate
    else -> R.string.value_invalid
}
