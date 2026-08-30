package org.saudigitus.semis.performance.performanceevent.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.saudigitus.semis.core.data.model.SearchTeiModel
import org.saudigitus.semis.core.designsystem.components.cards.TeiLearnerCard
import org.saudigitus.semis.core.designsystem.theme.dark_warning
import org.saudigitus.semis.core.designsystem.theme.light_error
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

    val record = formState.fieldsData
        .firstOrNull { it.tei == teiUid && it.dataElement == markField.dataElementUid }

    val ruleMessage = record?.errorMessage ?: record?.warningMessage

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
    TeiLearnerCard(
        tei = learner,
        maxAdditionalInfo = 0,
        trailing = {
            MarkInputField(
                value = record?.value.orEmpty(),
                enabled = formState.isEnabled && markField.enabled,
                hasError = record?.hasError == true,
                hasWarning = record?.hasWarning == true,
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

        // The colour of the box says a mark is being questioned; this says why, and says it under
        // the learner it concerns rather than somewhere the reader has to match up.
        ruleMessage?.takeIf { it.isNotBlank() }?.let { message ->
            Text(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = if (record?.hasError == true) light_error else dark_warning,
            )
        }
    }
}
