package org.saudigitus.semis.core.form.data

import androidx.annotation.StringRes
import org.hisp.dhis.android.core.arch.helpers.Result
import org.hisp.dhis.android.core.common.ValueType
import org.saudigitus.semis.core.designsystem.R

/**
 * Whether [value] can be stored as a value of [valueType], judged by the SDK's own validator.
 *
 * The server refuses a value its type does not allow, after the app has already said it was saved,
 * so the app judges it first. A blank value is never wrong here, since whether one is needed is
 * the business of the mandatory rule, and an unknown type is not judged.
 */
fun isValidValue(valueType: ValueType?, value: String?): Boolean =
    value.isNullOrBlank() ||
        valueType == null ||
        valueType.validator.validate(value) is Result.Success

/** What a value of [valueType] must look like, said to the person typing it. */
@StringRes
fun invalidValueMessage(valueType: ValueType): Int = when (valueType) {
    ValueType.INTEGER -> R.string.value_invalid_integer
    ValueType.INTEGER_POSITIVE -> R.string.value_invalid_integer_positive
    ValueType.INTEGER_NEGATIVE -> R.string.value_invalid_integer_negative
    ValueType.INTEGER_ZERO_OR_POSITIVE -> R.string.value_invalid_integer_zero_or_positive
    ValueType.NUMBER -> R.string.value_invalid_number
    ValueType.PERCENTAGE -> R.string.value_invalid_percentage
    ValueType.UNIT_INTERVAL -> R.string.value_invalid_unit_interval
    else -> R.string.value_invalid
}
