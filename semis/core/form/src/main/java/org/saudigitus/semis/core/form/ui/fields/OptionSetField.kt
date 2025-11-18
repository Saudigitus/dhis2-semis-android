package org.saudigitus.semis.core.form.ui.fields

import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import org.saudigitus.semis.core.designsystem.components.DropdownField
import org.saudigitus.semis.core.form.data.model.FormFieldState

@Composable
fun OptionSetField(
    field: FormFieldState,
    enabled: Boolean? = null,
    colors: TextFieldColors = TextFieldDefaults.colors(),
    onItemClick: (code: String) -> Unit
) {
    DropdownField(
        label = field.label + if (field.mandatory) " *" else "",
        placeholder = field.label,
        supportingText =field.errorMessage,
        isError = field.hasError,
        data = field.optionSet ?: emptyList(),
        selectedItem = null,
        enabled = enabled ?: field.enabled,
        colors = colors,
        onClick = {
            onItemClick.invoke(it.code.orEmpty())
        },
    )
}