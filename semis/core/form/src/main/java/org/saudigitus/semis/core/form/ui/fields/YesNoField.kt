package org.saudigitus.semis.core.form.ui.fields

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.saudigitus.semis.core.designsystem.R
import org.saudigitus.semis.core.form.data.model.FormFieldState
import org.saudigitus.semis.core.form.utils.FactoryData

@Composable
fun YesNoField(
    modifier: Modifier = Modifier,
    field: FormFieldState,
    onValueChange: (String) -> Unit
) {
    var selected by mutableStateOf("")

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = field.label + if (field.mandatory) " *" else "",
            style = MaterialTheme.typography.titleMedium,
            color = if (field.hasError) MaterialTheme.colorScheme.error else LocalContentColor.current
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start)
        ) {
            items(FactoryData.YES_NO) {
                RadioButton(
                    selected = selected == it.second,
                    onClick = {
                        selected = it.second
                        onValueChange(it.second)
                    },
                    enabled = field.enabled
                )
            }
        }
        if (field.mandatory && selected.isEmpty()) {
            Text(
                text = stringResource(id = R.string.yes_no_mandatory_error),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}