package org.saudigitus.campaign.core.form.ui.fields

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing
import org.saudigitus.campaign.core.designsystem.components.CustomDatePicker
import org.saudigitus.campaign.core.form.R
import org.saudigitus.campaign.core.form.data.models.FormFieldModel
import org.saudigitus.campaign.core.form.utils.Utils
import org.saudigitus.campaign.core.utils.DateHelper


@Composable
fun DateField(
    modifier: Modifier = Modifier,
    field: FormFieldModel,
    enable: Boolean? = null,
    colors: TextFieldColors = Utils.inputColors(),
    onValueChange: (String) -> Unit,
) {
    var displayCalendar by remember { mutableStateOf(false) }
    val isEnabled = (enable ?: field.enabled) == true

    CustomDatePicker(
        show = displayCalendar && isEnabled,
        dismiss = { displayCalendar = false },
        onDatePick = onValueChange
    )

    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(Spacing.Spacing0, 300.dp)
            .then(modifier),
        enabled = isEnabled,
        readOnly = true,
        value = DateHelper.formatDateWithWeekDay(field.value.orEmpty()).orEmpty(),
        onValueChange = onValueChange,
        label = { Text(text = field.label + if (field.mandatory == true) " *" else "") },
        placeholder = { Text(text = field.label) },

        trailingIcon = {
            IconButton(
                enabled = isEnabled,
                onClick = { displayCalendar = !displayCalendar }
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = field.label,
                )
            }
        },
        isError = field.hasError == true,
        supportingText = {
            FieldSupportingText(field)
        },
        singleLine = true,
        colors = colors,
    )
}

@Composable
fun DateField(
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    label: String = stringResource(R.string.registration_date),
    date: Long? = null,
    colors: TextFieldColors = Utils.inputColors(),
    onValueChange: (String) -> Unit,
) {
    var displayCalendar by remember { mutableStateOf(false) }
    var selectedDate by remember {
        mutableStateOf(
            DateHelper.formatDateWithWeekDay(
                DateHelper.formatDate(date ?: System.currentTimeMillis()).orEmpty()
            ).orEmpty()
        )
    }

    CustomDatePicker(
        show = displayCalendar && isEnabled,
        dismiss = { displayCalendar = false },
        date = date,
        onDatePick = {
            selectedDate = DateHelper.formatDateWithWeekDay(it).orEmpty()
            onValueChange(it)
        }
    )

    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(Spacing.Spacing0, 300.dp)
            .then(modifier),
        readOnly = true,
        value = selectedDate,
        onValueChange = onValueChange,
        label = { Text(text = label) },
        placeholder = { Text(text = label) },
        enabled = isEnabled,
        trailingIcon = {
            IconButton(
                enabled = isEnabled,
                onClick = { displayCalendar = !displayCalendar }
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = label,
                )
            }
        },
        singleLine = true,
        colors = colors,
    )
}
