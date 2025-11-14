package org.saudigitus.semis.core.form.ui.fields

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.saudigitus.semis.core.form.data.model.FormFieldState

@Composable
fun NumericField(
    field: FormFieldState,
    colors: TextFieldColors = TextFieldDefaults.colors(),
    onValueChange: (String) -> Unit
) {
    InputField(
        modifier = Modifier.fillMaxWidth(),
        field = field,
        colors = colors,
        onValueChange = {
            if (it.isEmpty() || it.matches("[-0-9.]+".toRegex())) onValueChange(it)
        },
    )
}