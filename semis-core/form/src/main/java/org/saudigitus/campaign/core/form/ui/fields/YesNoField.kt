package org.saudigitus.campaign.core.form.ui.fields

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import org.saudigitus.campaign.core.form.R
import org.saudigitus.campaign.core.form.data.models.FormFieldModel

@Composable
fun YesNoField(
    modifier: Modifier = Modifier,
    field: FormFieldModel,
    enabled: Boolean = true,
    onValueChange: (String) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = field.label + if (field.mandatory == true) " *" else "",
            style = MaterialTheme.typography.titleMedium,
            color = if (field.hasError == true) MaterialTheme.colorScheme.error else LocalContentColor.current
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start)
        ) {
            YesNoOption(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.semis_form_yes),
                value = "true",
                selected = field.value.isTrueValue(),
                enabled = enabled,
                onValueChange = onValueChange,
            )
            YesNoOption(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.semis_form_no),
                value = "false",
                selected = field.value.isFalseValue(),
                enabled = enabled,
                onValueChange = onValueChange,
            )
        }
    }
}

@Composable
private fun YesNoOption(
    modifier: Modifier,
    label: String,
    value: String,
    selected: Boolean,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline
            },
        ),
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .selectable(
                selected = selected,
                enabled = enabled,
                role = Role.RadioButton,
                onClick = { onValueChange(value) },
            ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.padding(end = 8.dp)) {
                RadioButton(
                    selected = selected,
                    enabled = enabled,
                    onClick = null,
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

private fun String?.isTrueValue(): Boolean =
    this?.trim()?.lowercase() in setOf("true", "1")

private fun String?.isFalseValue(): Boolean =
    this?.trim()?.lowercase() in setOf("false", "0")
