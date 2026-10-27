package org.saudigitus.semis.core.form.ui.fields

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import org.saudigitus.semis.core.form.data.model.FormFieldState

@Composable
fun TrueOnlyField(field: FormFieldState, onValueChange: (String) -> Unit) {
    var checked by remember { mutableStateOf(field.value == "true") }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = {
            checked = it
            onValueChange(if (it) "true" else "")
        })
        Text(field.label)
    }
}