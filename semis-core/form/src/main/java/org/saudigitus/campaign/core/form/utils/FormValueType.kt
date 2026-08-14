package org.saudigitus.campaign.core.form.utils

import org.hisp.dhis.android.core.common.ValueType

object FormValueType {
    val integer = listOf(
        ValueType.INTEGER,
        ValueType.INTEGER_NEGATIVE,
        ValueType.INTEGER_POSITIVE,
        ValueType.INTEGER_ZERO_OR_POSITIVE,
        ValueType.NUMBER,
    )

    val text = listOf(
        ValueType.TEXT,
        ValueType.LONG_TEXT,
        ValueType.MULTI_TEXT,
        ValueType.LETTER,
        ValueType.EMAIL
    )

    val date = listOf(
        ValueType.DATE,
        ValueType.TIME,
        ValueType.DATETIME,
    )
}