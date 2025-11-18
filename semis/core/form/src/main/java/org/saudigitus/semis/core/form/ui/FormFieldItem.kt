package org.saudigitus.semis.core.form.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.mobile.ui.designsystem.component.InputShellState
import org.saudigitus.semis.core.designsystem.attendance.AttendanceButton
import org.saudigitus.semis.core.designsystem.attendance.AttendanceButtonState
import org.saudigitus.semis.core.designsystem.attendance.model.AttendanceButtonModel
import org.saudigitus.semis.core.form.data.model.FormFieldState
import org.saudigitus.semis.core.form.ui.fields.InputField
import org.saudigitus.semis.core.form.ui.fields.NumericField
import org.saudigitus.semis.core.form.ui.fields.OptionSetField
import org.saudigitus.semis.core.form.ui.fields.TrueOnlyField
import org.saudigitus.semis.core.form.ui.fields.YesNoField
import org.saudigitus.semis.core.form.utils.FactoryData

@Composable
fun FormFieldItem(
    key: String,
    field: FormFieldState,
    enabled: Boolean? = null,
    attendanceButtonState: AttendanceButtonState = AttendanceButtonState(),
    colors: TextFieldColors = TextFieldDefaults.colors(
        focusedIndicatorColor = InputShellState.FOCUSED.color,
        unfocusedIndicatorColor = InputShellState.UNFOCUSED.color,
        disabledIndicatorColor = InputShellState.DISABLED.color,
    ),
    onAttendanceChange: (AttendanceButtonModel) -> Unit = {},
    onValueChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when {
            field.optionSet != null -> {
                if (field.isAttendanceType) {
                    AttendanceButton(
                        key = key,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        state = attendanceButtonState,
                        onClick = onAttendanceChange
                    )
                } else {
                    OptionSetField(
                        field,
                        enabled = enabled,
                        colors =
                            TextFieldDefaults.colors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = InputShellState.UNFOCUSED.color,
                                disabledIndicatorColor = InputShellState.DISABLED.color,
                            ),
                        onValueChange
                    )
                }
            }
            field.valueType == ValueType.BOOLEAN -> YesNoField(
                field = field,
                onValueChange = onValueChange
            )

            field.valueType == ValueType.TRUE_ONLY -> TrueOnlyField(field, onValueChange)
            FactoryData.NUMERIC_TYPES.contains(field.valueType) -> NumericField(
                field,
                colors,
                onValueChange
            )
            else -> InputField(field = field, colors = colors, onValueChange = onValueChange)
        }
    }
}