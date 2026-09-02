package org.saudigitus.campaign.core.form.rules

import org.hisp.dhis.android.core.program.ProgramRuleActionType
import org.hisp.dhis.rules.models.RuleEffect
import org.saudigitus.campaign.core.form.data.models.FormSectionModel

/**
 * Applies the outcome of a rule evaluation to these sections.
 *
 * Only the action types the enrollment form can express are honoured; anything else is left to
 * the server side of the rule engine. The semantics follow the ones the inherited capture app
 * applies, so a rule behaves the same here as it does there:
 *
 * - a field a rule hides stays visible when the configuration itself marks it mandatory, because
 *   hiding a required answer would let the record be written without it;
 * - a field a rule assigns shows the computed value and stops being editable, so the user cannot
 *   contradict what the server will enforce on import;
 * - a rule error does not displace an error the form already raised for the field, which sits
 *   closer to the keystroke that caused it.
 *
 * The receiver is expected to carry base state: every flag as the configuration defines it, with
 * no outcome of a previous evaluation left in, so that an effect whose condition stopped holding
 * disappears by simply not being applied again.
 */
internal fun List<FormSectionModel>.applyRuleEffects(
    effects: List<RuleEffect>,
): List<FormSectionModel> {
    if (effects.isEmpty()) return this

    val hiddenFields = mutableSetOf<String>()
    val hiddenSections = mutableSetOf<String>()
    val mandatoryFields = mutableSetOf<String>()
    val assignments = mutableMapOf<String, String?>()
    val errors = mutableMapOf<String, String>()
    val warnings = mutableMapOf<String, String>()

    effects.forEach { effect ->
        val action = effect.ruleAction
        when (action.type) {
            ProgramRuleActionType.HIDEFIELD.name ->
                action.field()?.let(hiddenFields::add)

            ProgramRuleActionType.HIDESECTION.name ->
                action.values["programStageSection"]?.let(hiddenSections::add)

            ProgramRuleActionType.SETMANDATORYFIELD.name ->
                action.field()?.let(mandatoryFields::add)

            ProgramRuleActionType.ASSIGN.name ->
                action.field()?.takeIf { it.isNotEmpty() }
                    ?.let { field -> assignments[field] = effect.data }

            ProgramRuleActionType.SHOWERROR.name ->
                action.field()?.let { field ->
                    errors[field] = supportingMessage(action.content(), effect.data)
                }

            ProgramRuleActionType.SHOWWARNING.name ->
                action.field()?.let { field ->
                    warnings[field] = supportingMessage(action.content(), effect.data)
                }

            // Every other action type has no surface on this form.
            else -> Unit
        }
    }

    return map { section ->
        section.copy(
            rendered = section.rendered && section.uid !in hiddenSections,
            formFields = section.formFields.map { field ->
                var updated = field

                if (field.uid in hiddenFields && field.baseMandatory != true) {
                    updated = updated.copy(rendered = false)
                }
                if (field.uid in mandatoryFields) {
                    updated = updated.copy(mandatory = true)
                }
                if (field.uid in assignments) {
                    updated = updated.copy(value = assignments[field.uid], enabled = false)
                }
                errors[field.uid]?.let { message ->
                    if (updated.hasError != true) {
                        updated = updated.copy(hasError = true, errorMessage = message)
                    }
                }
                warnings[field.uid]?.let { message ->
                    updated = updated.copy(hasWarning = true, warningMessage = message)
                }

                updated
            },
        )
    }
}

/**
 * What the user reads under the field: the configured text, followed by the evaluated data when
 * the rule computed one, which is how the inherited capture app composes it.
 */
private fun supportingMessage(content: String?, data: String?): String =
    listOfNotNull(content, data)
        .map(String::trim)
        .filter(String::isNotEmpty)
        .joinToString(" ")
