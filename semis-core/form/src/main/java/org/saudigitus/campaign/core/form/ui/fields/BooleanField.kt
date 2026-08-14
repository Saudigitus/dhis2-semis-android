package org.saudigitus.campaign.core.form.ui.fields

/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import org.saudigitus.campaign.core.data.models.OptionModel
import org.saudigitus.campaign.core.form.data.models.FormFieldModel


@Composable
fun BooleanField(
    modifier: Modifier = Modifier,
    field: FormFieldModel,
    onValueChange: (String) -> Unit
) {
    var selected by remember { mutableStateOf("") }

    Surface(
        shape = MaterialTheme.shapes.small,
        color = if (selected == field.value) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface.copy(.1f)
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (selected == field.value) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline
            }
        ),
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .selectable(
                selected == field.value,
                onClick = {
                    onValueChange.invoke(field.value.orEmpty())
                },
                role = Role.RadioButton
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.padding(8.dp)) {
                RadioButton(selected == field.value, onClick = null)
            }
            Text(field.label, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun BooleanField(
    modifier: Modifier = Modifier,
    option: OptionModel,
    onValueChange: (String) -> Unit
) {
    var selected by remember { mutableStateOf("") }

    Surface(
        shape = MaterialTheme.shapes.small,
        color = if (selected == option.code) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface.copy(.1f)
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (selected == option.code) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline
            }
        ),
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .selectable(
                selected == option.code,
                onClick = {
                    onValueChange.invoke(option.code.orEmpty())
                },
                role = Role.RadioButton
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.padding(8.dp)) {
                RadioButton(selected == option.code, onClick = null)
            }
            Text(option.displayName.orEmpty(),  style = MaterialTheme.typography.bodyLarge)
        }
    }
}