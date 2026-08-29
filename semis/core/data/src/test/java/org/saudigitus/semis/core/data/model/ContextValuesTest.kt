package org.saudigitus.semis.core.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContextValuesTest {

    private val context = listOf(
        "iDSrFrrVgmX" to "2025",
        "kNNoif9gASf" to "Grade 1",
        "RhABRLO2Fae" to "A",
    )

    @Test
    fun `a stage holding every configured value carries them all`() {
        val stage = listOf("d0MKWRNGv0a", "oLUMMT84ILM", "iDSrFrrVgmX", "kNNoif9gASf", "RhABRLO2Fae")

        assertEquals(context, contextValuesHeldBy(stage, context))
    }

    @Test
    fun `a stage holding some of them carries only those`() {
        val stage = listOf("d0MKWRNGv0a", "kNNoif9gASf")

        assertEquals(listOf("kNNoif9gASf" to "Grade 1"), contextValuesHeldBy(stage, context))
    }

    @Test
    fun `a stage holding none of them is left alone`() {
        val stage = listOf("d0MKWRNGv0a", "oLUMMT84ILM")

        assertTrue(contextValuesHeldBy(stage, context).isEmpty())
    }

    @Test
    fun `a stage with nothing configured is left alone`() {
        assertTrue(contextValuesHeldBy(emptyList(), context).isEmpty())
    }

    @Test
    fun `nothing configured as context writes nothing`() {
        val stage = listOf("d0MKWRNGv0a", "iDSrFrrVgmX")

        assertTrue(contextValuesHeldBy(stage, emptyList()).isEmpty())
    }

    @Test
    fun `a value that is blank is not written, since it says nothing`() {
        val stage = listOf("iDSrFrrVgmX", "kNNoif9gASf")
        val partial = listOf("iDSrFrrVgmX" to "2025", "kNNoif9gASf" to "")

        assertEquals(listOf("iDSrFrrVgmX" to "2025"), contextValuesHeldBy(stage, partial))
    }

    @Test
    fun `the order the configuration gives is kept`() {
        val stage = listOf("RhABRLO2Fae", "kNNoif9gASf", "iDSrFrrVgmX")

        assertEquals(context, contextValuesHeldBy(stage, context))
    }
}
