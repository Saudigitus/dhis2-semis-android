package org.saudigitus.campaign.core.form.ui.fields

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import org.saudigitus.campaign.core.form.data.models.FormFieldModel

@Composable
fun FieldSupportingText(field: FormFieldModel) {
    Column {
        field.errorMessage?.takeIf { it.isNotBlank() }?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error
            )
        }

        if (field.hasWarning == true) {
            field.warningMessage?.takeIf { it.isNotBlank() }?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}
