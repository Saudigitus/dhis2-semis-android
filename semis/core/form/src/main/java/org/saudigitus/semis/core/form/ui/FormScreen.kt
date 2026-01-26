package org.saudigitus.semis.core.form.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.hisp.dhis.mobile.ui.designsystem.component.ProgressIndicator
import org.hisp.dhis.mobile.ui.designsystem.component.ProgressIndicatorType
import org.saudigitus.semis.core.data.model.SearchTeiModel
import org.saudigitus.semis.core.form.data.model.FormType
import org.saudigitus.semis.core.form.ui.state.FormEvent
import org.saudigitus.semis.core.form.ui.state.FormUiState


@Composable
fun FormContent(
    key: String,
    tei: SearchTeiModel? = null,
    type: FormType,
    modifier: Modifier = Modifier,
    state: FormUiState,
    onEvent: (FormEvent) -> Unit
) {
    Box(modifier = modifier) {
        when {
            state.isLoading -> {
                ProgressIndicator(
                    type = ProgressIndicatorType.CIRCULAR_SMALL,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            state.error != null -> {
                Text(
                    "Error: ${state.error}",
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            else -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    for (field in state.fields) {
                        FormFieldItem(
                            key = key,
                            field = field,
                            fieldsData = state.fieldsData,
                            enabled = if (type == FormType.ATTENDANCE) {
                                state.attendanceButtonState.isEditing
                            } else state.isEnabled,
                            attendanceButtonState = state.attendanceButtonState,
                            onAttendanceChange = { onEvent(FormEvent.UpdateAttendance(tei, it)) },
                            onValueChange = { value ->
                                onEvent(
                                    FormEvent.UpdateField(
                                        type,
                                        tei?.tei?.uid().orEmpty(),
                                        field.dataElementUid,
                                        value
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}