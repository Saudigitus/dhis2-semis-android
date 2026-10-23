package org.saudigitus.semis.core.form.utils

import org.hisp.dhis.android.core.common.ValueType
import org.junit.Assert.assertEquals
import org.junit.Test

class FactoryDataTest {

    @Test
    fun `every value type the SDK calls numeric is a numeric field`() {
        assertEquals(
            ValueType.entries.filter { it.isNumeric }.toSet(),
            FactoryData.NUMERIC_TYPES,
        )
    }
}
