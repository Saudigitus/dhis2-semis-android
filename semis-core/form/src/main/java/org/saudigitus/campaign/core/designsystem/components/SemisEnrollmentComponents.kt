package org.saudigitus.campaign.core.designsystem.components

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import org.saudigitus.campaign.core.utils.DateHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDatePicker(
    show: Boolean = false,
    dismiss: () -> Unit,
    onDatePick: (String) -> Unit,
    date: Long? = null,
    dateValidator: (Long) -> Boolean = { true },
) {
    if (!show) return
    val state = rememberDatePickerState(initialSelectedDateMillis = date)
    DatePickerDialog(
        onDismissRequest = dismiss,
        confirmButton = {
            TextButton(onClick = {
                onDatePick(DateHelper.formatDate(state.selectedDateMillis ?: System.currentTimeMillis()).orEmpty())
                dismiss()
            }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = dismiss) { Text("Cancel") } },
    ) { DatePicker(state = state, title = {}) }
}
