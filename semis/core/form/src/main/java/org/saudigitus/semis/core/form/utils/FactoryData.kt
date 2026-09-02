package org.saudigitus.semis.core.form.utils

import org.hisp.dhis.android.core.common.ValueType

object FactoryData {
    val YES_NO = listOf(Pair("Yes","true"), Pair("No", "false"))
    
    val NUMERIC_TYPES = setOf(
        ValueType.INTEGER,
        ValueType.INTEGER_POSITIVE,
        ValueType.INTEGER_NEGATIVE,
        ValueType.NUMBER,
        ValueType.UNIT_INTERVAL,
        ValueType.PERCENTAGE
    )

    val DATE_TYPES = setOf(
        ValueType.DATE,
        ValueType.DATETIME
    )

    val TEXT_TYPES = setOf(
        ValueType.TEXT,
        ValueType.LONG_TEXT,
        ValueType.MULTI_TEXT
    )
}