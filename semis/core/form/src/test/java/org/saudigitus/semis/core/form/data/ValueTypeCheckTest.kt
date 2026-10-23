package org.saudigitus.semis.core.form.data

import org.hisp.dhis.android.core.common.ValueType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.saudigitus.semis.core.designsystem.R

class ValueTypeCheckTest {

    @Test
    fun `a zero or positive mark takes whole numbers from zero up`() {
        assertTrue(isValidValue(ValueType.INTEGER_ZERO_OR_POSITIVE, "0"))
        assertTrue(isValidValue(ValueType.INTEGER_ZERO_OR_POSITIVE, "72"))
        assertFalse(isValidValue(ValueType.INTEGER_ZERO_OR_POSITIVE, "-5"))
        assertFalse(isValidValue(ValueType.INTEGER_ZERO_OR_POSITIVE, "7.5"))
        assertFalse(isValidValue(ValueType.INTEGER_ZERO_OR_POSITIVE, "7,5"))
    }

    @Test
    fun `a blank value or an unknown type is not judged`() {
        assertTrue(isValidValue(ValueType.INTEGER_ZERO_OR_POSITIVE, null))
        assertTrue(isValidValue(ValueType.INTEGER_ZERO_OR_POSITIVE, ""))
        assertTrue(isValidValue(null, "anything"))
    }

    @Test
    fun `the message names what the type allows`() {
        assertEquals(
            R.string.value_invalid_integer_zero_or_positive,
            invalidValueMessage(ValueType.INTEGER_ZERO_OR_POSITIVE),
        )
        assertEquals(R.string.value_invalid, invalidValueMessage(ValueType.EMAIL))
    }
}
