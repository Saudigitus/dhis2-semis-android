package org.saudigitus.campaign.core.form.ui.fields

import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.saudigitus.campaign.core.form.data.models.FormFieldModel
import org.saudigitus.campaign.core.form.utils.Utils

@Composable
fun NumericField(
    modifier: Modifier = Modifier,
    field: FormFieldModel,
    enabled: Boolean? = null,
    colors: TextFieldColors = Utils.inputColors(),
    onValueChange: (String) -> Unit
) {
    InputField(
        modifier = modifier,
        field = field,
        enable = enabled,
        colors = colors,
        onValueChange = {
            if (it.isEmpty() || it.matches("[-0-9.]+".toRegex())) onValueChange(it)
        },
    )
}