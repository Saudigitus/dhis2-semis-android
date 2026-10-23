package org.saudigitus.campaign.core.form.utils

import org.hisp.dhis.android.core.common.ValueType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.saudigitus.campaign.core.form.data.models.FormFieldModel
import org.saudigitus.campaign.core.form.data.models.FormSectionModel

class ValueTypeCheckTest {

    private fun field(
        valueType: ValueType?,
        value: String?,
        mandatory: Boolean = false,
        hasError: Boolean = false,
        errorMessage: String? = null,
    ) = FormFieldModel(
        uid = "field",
        label = "Field",
        valueType = valueType,
        value = value,
        mandatory = mandatory,
        hasError = hasError,
        errorMessage = errorMessage,
    )

    private fun section(vararg fields: FormFieldModel) = FormSectionModel(
        uid = "section-1",
        code = null,
        name = "Section",
        formFields = fields.toList(),
    )

    private val message: (Int) -> String = { "type $it" }

    @Test
    fun `numbers are judged by their exact type`() {
        assertTrue(field(ValueType.INTEGER_POSITIVE, "3").hasValueOfItsType())
        assertFalse(field(ValueType.INTEGER_POSITIVE, "0").hasValueOfItsType())
        assertFalse(field(ValueType.INTEGER_POSITIVE, "-3").hasValueOfItsType())
        assertFalse(field(ValueType.INTEGER, "1.5").hasValueOfItsType())
        assertFalse(field(ValueType.INTEGER, "-").hasValueOfItsType())
        assertTrue(field(ValueType.INTEGER_ZERO_OR_POSITIVE, "0").hasValueOfItsType())
        assertFalse(field(ValueType.PERCENTAGE, "101").hasValueOfItsType())
        assertTrue(field(ValueType.NUMBER, "1.5").hasValueOfItsType())
    }

    @Test
    fun `text shaped types are judged too`() {
        assertTrue(field(ValueType.PHONE_NUMBER, "+220 7012345").hasValueOfItsType())
        assertFalse(field(ValueType.PHONE_NUMBER, "call me").hasValueOfItsType())
        assertTrue(field(ValueType.EMAIL, "head@school.gm").hasValueOfItsType())
        assertFalse(field(ValueType.EMAIL, "head.school.gm").hasValueOfItsType())
        assertFalse(field(ValueType.LETTER, "AB").hasValueOfItsType())
        assertTrue(field(ValueType.DATE, "2019-03-14").hasValueOfItsType())
        assertFalse(field(ValueType.DATE, "14/03/2019").hasValueOfItsType())
    }

    @Test
    fun `a blank value or a field without a type is never wrong`() {
        assertTrue(field(ValueType.INTEGER, null).hasValueOfItsType())
        assertTrue(field(ValueType.INTEGER, "  ").hasValueOfItsType())
        assertTrue(field(null, "anything").hasValueOfItsType())
    }

    @Test
    fun `a wrong value is flagged with what its type expects`() {
        val checked = field(ValueType.INTEGER_POSITIVE, "-3").withValueTypeChecked(message)

        assertEquals(true, checked.hasError)
        assertEquals(message(ValueType.INTEGER_POSITIVE.invalidValueMessage()), checked.errorMessage)
    }

    @Test
    fun `correcting the value clears the flag the type raised`() {
        val wrong = field(ValueType.INTEGER_POSITIVE, "-3").withValueTypeChecked(message)
        val corrected = wrong.copy(value = "3").withValueTypeChecked(message)

        assertEquals(false, corrected.hasError)
        assertNull(corrected.errorMessage)
    }

    @Test
    fun `an error raised by a rule is left to the rules`() {
        val ruled = field(
            ValueType.INTEGER_POSITIVE,
            "3",
            hasError = true,
            errorMessage = "Older than the school allows",
        ).withValueTypeChecked(message)

        assertEquals(true, ruled.hasError)
        assertEquals("Older than the school allows", ruled.errorMessage)
    }

    @Test
    fun `a wrong value keeps the step from moving on even before the rules run`() {
        val blocked = section(field(ValueType.INTEGER_POSITIVE, "-3"))

        assertEquals(0, blocked.firstBlockingFieldIndex())
        assertTrue(listOf(blocked).hasBlockingFields())
    }

    @Test
    fun `only a required question left empty is missing its answer`() {
        assertTrue(field(ValueType.DATE, null, mandatory = true).isMissingMandatoryAnswer())
        assertFalse(field(ValueType.TEXT, "QA", mandatory = true).isMissingMandatoryAnswer())
        assertFalse(field(ValueType.TEXT, null, mandatory = false).isMissingMandatoryAnswer())
    }
}
