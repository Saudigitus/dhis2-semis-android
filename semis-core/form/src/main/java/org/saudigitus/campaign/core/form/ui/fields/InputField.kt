package org.saudigitus.campaign.core.form.ui.fields

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import org.dhis2.composetable.model.extensions.keyboardCapitalization
import org.dhis2.composetable.model.extensions.toKeyboardType
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing
import org.saudigitus.campaign.core.designsystem.utils.Utils
import org.saudigitus.campaign.core.form.data.models.FormFieldModel
import org.saudigitus.campaign.core.form.utils.toKeyBoardInputType


@Composable
fun InputField(
    modifier: Modifier = Modifier,
    field: FormFieldModel,
    enable: Boolean? = null,
    onValueChange: (String) -> Unit,
    colors: TextFieldColors = Utils.inputColors(),
) {
    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(Spacing.Spacing0, 300.dp)
            .then(modifier),
        enabled = (enable ?: field.enabled) == true,
        value = field.value.orEmpty(),
        onValueChange = onValueChange,
        label = { Text(text = field.label + if (field.mandatory == true) " *" else "") },
        placeholder = { Text(text = field.label) },
        leadingIcon = {
            when (field.valueType?.toKeyBoardInputType()?.toKeyboardType()) {
                KeyboardType.Email -> {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = field.label,
                    )
                }

                KeyboardType.Text -> {
                    Icon(
                        imageVector = Icons.Default.TextFields,
                        contentDescription = field.label,
                    )
                }

                KeyboardType.Number, KeyboardType.Decimal, KeyboardType.NumberPassword -> {
                    Icon(
                        imageVector = Icons.Default.Numbers,
                        contentDescription = field.label,
                    )
                }
            }
        },
        isError = field.hasError == true,
        supportingText = {
            FieldSupportingText(field)
        },
        singleLine = field.valueType?.toKeyBoardInputType()?.multiline == true,
        maxLines = if (field.valueType?.toKeyBoardInputType()?.multiline == false) 1 else Int.MAX_VALUE,
        keyboardOptions = KeyboardOptions(
            keyboardType = field.valueType?.toKeyBoardInputType()?.toKeyboardType()
                ?: KeyboardType.Text,
            capitalization = field.valueType?.toKeyBoardInputType()?.keyboardCapitalization()
                ?: KeyboardCapitalization.None,
            imeAction = ImeAction.Done,
        ),
        colors = colors,
        visualTransformation = if (field.valueType?.toKeyBoardInputType()
                ?.toKeyboardType() == KeyboardType.Password
        ) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
    )
}
