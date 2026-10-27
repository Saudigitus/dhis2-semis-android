package org.saudigitus.campaign.core.form.ui.fields

import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import org.saudigitus.campaign.core.form.data.models.FormFieldModel
import org.saudigitus.campaign.core.form.utils.CustomValueType
import org.saudigitus.campaign.core.form.utils.Utils

@Composable
fun OptionSetField(
    field: FormFieldModel,
    enabled: Boolean? = null,
    colors: TextFieldColors = Utils.inputColors(),
    onItemClick: (code: String) -> Unit
) {
    val options = field.optionSet.orEmpty()
    var selectedCode by rememberSaveable(field.uid) { mutableStateOf(field.value) }
    val selectedItem = options.find { it.code == selectedCode }

    LaunchedEffect(field.uid, options) {
        val singleOptionCode = options.singleOrNull()?.code
        if (
            field.customValueType == CustomValueType.DROPDOWN &&
            selectedCode.isNullOrBlank() &&
            !singleOptionCode.isNullOrBlank()
        ) {
            selectedCode = singleOptionCode
            onItemClick(singleOptionCode)
        }
    }

    DropdownField(
        label = field.label + if (field.mandatory == true) " *" else "",
        placeholder = field.label,
        supportingText = if (field.hasSupportingMessages) {
            { FieldSupportingText(field) }
        } else {
            null
        },
        isError = field.hasError == true,
        data = options,
        selectedItem = selectedItem,
        enabled = (enabled ?: field.enabled) == true,
        colors = colors,
        onClick = { option ->
            val code = option.code.orEmpty()
            selectedCode = code
            onItemClick(code)
        },
    )
}
