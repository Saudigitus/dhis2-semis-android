package org.saudigitus.semis.performance.performanceevent.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.saudigitus.semis.core.data.model.SearchTeiModel
import org.saudigitus.semis.core.designsystem.components.cards.TeiLearnerCard
import org.saudigitus.semis.core.form.data.model.FormFieldState
import org.saudigitus.semis.core.form.data.model.FormType
import org.saudigitus.semis.core.form.ui.FormContent
import org.saudigitus.semis.core.form.ui.state.FormEvent
import org.saudigitus.semis.core.form.ui.state.FormUiState

/**
 * Roster row of the marks list: the learner card with the mark typed at the end of the row.
 *
 * [markField] is only set when the stage captures a single numeric data element, which is the
 * case this compact row is meant for. Any other form is rendered underneath the learner, as the
 * fields there need the full width.
 */
@Composable
internal fun PerformanceStudentRow(
    learner: SearchTeiModel,
    formState: FormUiState,
    markField: FormFieldState?,
    modifier: Modifier = Modifier,
    onFormEvent: (FormEvent) -> Unit,
) {
    val teiUid = learner.tei.uid()

    if (markField == null) {
        TeiLearnerCard(
            tei = learner,
            modifier = modifier,
            maxAdditionalInfo = 0,
        ) {
            FormContent(
                key = learner.uid(),
                tei = learner,
                type = FormType.INDIVIDUAL,
                modifier = Modifier.fillMaxWidth(),
                state = formState,
                onEvent = onFormEvent,
            )
        }
        return
    }

    val value = formState.fieldsData
        .firstOrNull { it.tei == teiUid && it.dataElement == markField.dataElementUid }
        ?.value
        .orEmpty()

    TeiLearnerCard(
        tei = learner,
        modifier = modifier,
        maxAdditionalInfo = 0,
        trailing = {
            MarkInputField(
                value = value,
                enabled = formState.isEnabled && markField.enabled,
                onValueChange = { mark ->
                    onFormEvent(
                        FormEvent.UpdateField(
                            FormType.INDIVIDUAL,
                            teiUid,
                            markField.dataElementUid,
                            mark,
                        )
                    )
                },
            )
        },
    )
}
