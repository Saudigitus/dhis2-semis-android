package org.saudigitus.campaign.core.form.rules

import org.hisp.dhis.android.core.program.ProgramRuleActionType
import org.hisp.dhis.rules.models.RuleAction
import org.hisp.dhis.rules.models.RuleEffect
import org.junit.Assert.assertEquals
import org.junit.Test
import org.saudigitus.campaign.core.form.data.models.FormFieldModel
import org.saudigitus.campaign.core.form.data.models.FormSectionModel

class RuleEffectApplicationTest {

    private fun section(vararg fields: FormFieldModel) = FormSectionModel(
        uid = "section-1",
        code = null,
        name = "Section",
        formFields = fields.toList(),
    )

    private fun field(
        uid: String,
        mandatory: Boolean = false,
        hasError: Boolean = false,
    ) = FormFieldModel(
        uid = uid,
        label = uid,
        mandatory = mandatory,
        baseMandatory = mandatory,
        hasError = hasError,
        errorMessage = if (hasError) "typed wrong" else null,
    )

    private fun effect(
        type: ProgramRuleActionType,
        values: Map<String, String>,
        data: String? = null,
    ) = RuleEffect(
        ruleId = "rule",
        ruleAction = RuleAction(data = data, type = type.name, values = values),
        data = data,
    )

    @Test
    fun `a hidden field stops being rendered`() {
        val sections = listOf(section(field("a"), field("b"))).applyRuleEffects(
            listOf(effect(ProgramRuleActionType.HIDEFIELD, mapOf("field" to "a"))),
        )

        assertEquals(false, sections.first().formFields[0].rendered)
        assertEquals(true, sections.first().formFields[1].rendered)
    }

    @Test
    fun `a field the configuration marks mandatory refuses to hide`() {
        val sections = listOf(section(field("a", mandatory = true))).applyRuleEffects(
            listOf(effect(ProgramRuleActionType.HIDEFIELD, mapOf("field" to "a"))),
        )

        assertEquals(true, sections.first().formFields[0].rendered)
    }

    @Test
    fun `a hidden section stops being rendered`() {
        val sections = listOf(section(field("a"))).applyRuleEffects(
            listOf(
                effect(
                    ProgramRuleActionType.HIDESECTION,
                    mapOf("programStageSection" to "section-1"),
                ),
            ),
        )

        assertEquals(false, sections.first().rendered)
    }

    @Test
    fun `an assigned field carries the computed value and stops being editable`() {
        val sections = listOf(section(field("a"))).applyRuleEffects(
            listOf(effect(ProgramRuleActionType.ASSIGN, mapOf("field" to "a"), data = "true")),
        )

        val assigned = sections.first().formFields[0]
        assertEquals("true", assigned.value)
        assertEquals(false, assigned.enabled)
    }

    @Test
    fun `a rule error reads as the configured text followed by the evaluated data`() {
        val sections = listOf(section(field("a"))).applyRuleEffects(
            listOf(
                effect(
                    ProgramRuleActionType.SHOWERROR,
                    mapOf("field" to "a", "content" to "Check the value"),
                    data = "150",
                ),
            ),
        )

        val flagged = sections.first().formFields[0]
        assertEquals(true, flagged.hasError)
        assertEquals("Check the value 150", flagged.errorMessage)
    }

    @Test
    fun `a rule error does not displace the error the form already raised`() {
        val sections = listOf(section(field("a", hasError = true))).applyRuleEffects(
            listOf(
                effect(
                    ProgramRuleActionType.SHOWERROR,
                    mapOf("field" to "a", "content" to "Rule error"),
                ),
            ),
        )

        assertEquals("typed wrong", sections.first().formFields[0].errorMessage)
    }

    @Test
    fun `a warning lands on the field without blocking it`() {
        val sections = listOf(section(field("a"))).applyRuleEffects(
            listOf(
                effect(
                    ProgramRuleActionType.SHOWWARNING,
                    mapOf("field" to "a", "content" to "Learner is under 3"),
                ),
            ),
        )

        val flagged = sections.first().formFields[0]
        assertEquals(true, flagged.hasWarning)
        assertEquals("Learner is under 3", flagged.warningMessage)
        assertEquals(false, flagged.hasError)
    }

    @Test
    fun `a rule can make a field mandatory`() {
        val sections = listOf(section(field("a"))).applyRuleEffects(
            listOf(effect(ProgramRuleActionType.SETMANDATORYFIELD, mapOf("field" to "a"))),
        )

        assertEquals(true, sections.first().formFields[0].mandatory)
    }

    @Test
    fun `an action type with no surface on this form changes nothing`() {
        val before = listOf(section(field("a")))
        val after = before.applyRuleEffects(
            listOf(effect(ProgramRuleActionType.DISPLAYTEXT, mapOf("content" to "note"))),
        )

        assertEquals(before, after)
    }
}
