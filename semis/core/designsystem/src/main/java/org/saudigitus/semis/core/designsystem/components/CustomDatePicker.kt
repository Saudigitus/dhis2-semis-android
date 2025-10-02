package org.saudigitus.semis.core.designsystem.components

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import org.dhis2.commons.date.DateUtils
import org.dhis2.ui.theme.colorPrimary
import org.saudigitus.semis.core.designsystem.R
import org.saudigitus.semis.core.designsystem.theme.light_error
import org.saudigitus.semis.core.designsystem.theme.light_info
import org.saudigitus.semis.core.utils.DateHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDatePicker(
    show: Boolean = false,
    dismiss: () -> Unit,
    onDatePick: (date: String) -> Unit,
    dateValidator: (Long) -> Boolean = { true },
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = null,
        initialDisplayMode = DisplayMode.Picker,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return dateValidator(utcTimeMillis)
            }
        },
    )

    var selectedDate by remember {
        mutableStateOf(
            DateHelper.formatDate(
                datePickerState.selectedDateMillis ?: DateUtils.getInstance().today.time,
            ) ?: "",
        )
    }

    if (show) {
        DatePickerDialog(
            onDismissRequest = {},
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedDate = DateHelper.formatDate(datePickerState.selectedDateMillis ?: 0) ?: ""
                        onDatePick.invoke(selectedDate)
                        dismiss.invoke()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = light_info,
                    ),
                    content = {
                        Text(stringResource(R.string.done))
                    }
                )
            },
            dismissButton = {
                TextButton(
                    onClick = dismiss::invoke,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = light_error,
                    ),
                    content = {
                        Text(stringResource(R.string.cancel))
                    }
                )
            },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = true,
            ),
            colors = DatePickerDefaults.colors(
                containerColor = Color.White,
                todayContentColor = colorPrimary,
                todayDateBorderColor = colorPrimary,
                selectedDayContainerColor = colorPrimary,
                selectedYearContainerColor = colorPrimary,
            ),
        ) {
            DatePicker(
                state = datePickerState,
                title = {},
                showModeToggle = false,
            )
        }
    }
}