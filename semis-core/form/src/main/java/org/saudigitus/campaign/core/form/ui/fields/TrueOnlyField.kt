package org.saudigitus.campaign.core.form.ui.fields

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import org.hisp.dhis.mobile.ui.designsystem.component.InputShellState
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.saudigitus.campaign.core.form.data.models.FormFieldModel

@Composable
fun TrueOnlyField(
    modifier: Modifier = Modifier,
    field: FormFieldModel,
    enable: Boolean? = null,
    onValueChange: (String) -> Unit
) {

    var selected by remember { mutableStateOf(field.value?.toBoolean() ?: false) }

    Surface(
        shape = MaterialTheme.shapes.small,
        color = when {
            field.enabled != true -> InputShellState.DISABLED.color.copy(.1f)
            selected || field.value.toBoolean() -> SurfaceColor.Surface
            else -> MaterialTheme.colorScheme.surface.copy(.1f)
        },
        border = BorderStroke(
            width = 0.75.dp,
            color = when {
                field.enabled != true -> InputShellState.DISABLED.color.copy(.1f)
                selected || field.value.toBoolean() -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.outline
            }
        ),
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .selectable(
                selected == field.value.toBoolean(),
                enabled = (enable ?: field.enabled) == true,
                onClick = {
                    selected = !selected
                    onValueChange(if (selected) true.toString() else "")
                },
                role = Role.Checkbox
            )
            .padding(vertical = 2.dp)
            .then(modifier)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = selected || field.value.toBoolean(),
                enabled = (enable ?: field.enabled) == true,
                onCheckedChange = {
                    selected = it
                    onValueChange(if (it) true.toString() else "")
                },
                colors = CheckboxDefaults.colors(
                    checkedColor = InputShellState.FOCUSED.color,
                )
            )
            Text(
                text = field.label + if (field.mandatory == true) " *" else "",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}