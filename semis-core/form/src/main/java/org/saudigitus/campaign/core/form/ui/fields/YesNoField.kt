package org.saudigitus.campaign.core.form.ui.fields

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.saudigitus.campaign.core.form.data.models.FormFieldModel

@Composable
fun YesNoField(
    modifier: Modifier = Modifier,
    field: FormFieldModel,
    onValueChange: (String) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = field.label + if (field.mandatory == true) " *" else "",
            style = MaterialTheme.typography.titleMedium,
            color = if (field.hasError == true) MaterialTheme.colorScheme.error else LocalContentColor.current
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start)
        ) {
            items (field.optionSet ?: emptyList(), key = { it.uid }) { option ->
                BooleanField(
                    option = option,
                    onValueChange = onValueChange
                )
            }
        }
    }
}