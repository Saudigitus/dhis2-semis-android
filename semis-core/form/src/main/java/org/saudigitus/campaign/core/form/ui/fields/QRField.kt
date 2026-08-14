package org.saudigitus.campaign.core.form.ui.fields

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.journeyapps.barcodescanner.ScanContract
import org.dhis2.composetable.model.extensions.keyboardCapitalization
import org.dhis2.composetable.model.extensions.toKeyboardType
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing
import org.saudigitus.campaign.core.data.models.QrResult
import org.saudigitus.campaign.core.designsystem.utils.Utils
import org.saudigitus.campaign.core.form.data.models.FormFieldModel
import org.saudigitus.campaign.core.form.utils.toKeyBoardInputType
import org.saudigitus.campaign.core.utils.Utils.scanOptions

@Composable
fun QRField(
    modifier: Modifier = Modifier,
    field: FormFieldModel,
    enable: Boolean? = null,
    onValueChange: (String) -> Unit,
    colors: TextFieldColors = Utils.inputColors(),
) {
    var resultName by rememberSaveable { mutableStateOf<String?>(null) }

    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ScanContract(),
    ) { result ->
        QrResult.fromJson(result.contents)
            ?.let { data ->
                resultName = data.displayName
                onValueChange(data.uid.orEmpty())
            }
    }


    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(Spacing.Spacing0, 300.dp)
            .then(modifier),
        enabled = (enable ?: field.enabled) == true,
        readOnly = true,
        value = resultName ?: field.value.orEmpty(),
        onValueChange = onValueChange,
        label = { Text(text = field.label + if (field.mandatory == true) " *" else "") },
        placeholder = { Text(text = field.label) },
        trailingIcon = {
            IconButton(
                onClick = { scannerLauncher.launch(scanOptions()) }
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = field.label
                )
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
