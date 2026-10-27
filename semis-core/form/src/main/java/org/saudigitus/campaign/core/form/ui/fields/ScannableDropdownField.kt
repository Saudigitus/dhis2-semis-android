package org.saudigitus.campaign.core.form.ui.fields

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.journeyapps.barcodescanner.ScanContract
import org.saudigitus.campaign.core.data.models.QrResult
import org.saudigitus.campaign.core.form.data.models.FormFieldModel
import org.saudigitus.campaign.core.form.utils.Utils
import org.saudigitus.campaign.core.utils.Utils.scanOptions

@Composable
fun ScannableDropdownField(
    field: FormFieldModel,
    enabled: Boolean? = null,
    colors: TextFieldColors = Utils.inputColors(),
    onItemClick: (code: String) -> Unit
) {
    var selectedCode by rememberSaveable(field.uid) { mutableStateOf(field.value) }
    val selectedItem = field.optionSet?.find { it.code == selectedCode }

    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ScanContract(),
    ) { result ->
        val scannedContent = result.contents.orEmpty()
        val scannedCode = QrResult.fromJson(scannedContent)?.uid
            ?.takeIf { it.isNotBlank() }
            ?: scannedContent.takeIf { it.isNotBlank() }

        scannedCode?.let { code ->
            field.optionSet
                ?.find { it.code == code }
                ?.let {
                    selectedCode = code
                    onItemClick(code)
                }
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
        data = field.optionSet ?: emptyList(),
        selectedItem = selectedItem,
        enabled = (enabled ?: field.enabled) == true,
        extraTrailingIcon = {
            IconButton(
                onClick = {
                    scannerLauncher.launch(scanOptions())
                }
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = null
                )
            }
        },
        colors = colors,
        onClick = { option ->
            val code = option.code.orEmpty()
            selectedCode = code
            onItemClick(code)
        },
    )
}
