package org.saudigitus.campaign.core.form.ui.fields

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.saudigitus.campaign.core.form.data.models.FormFieldModel
import org.saudigitus.campaign.core.form.utils.Utils

@Composable
fun CounterTextField(
    modifier: Modifier = Modifier,
    field: FormFieldModel,
    enable: Boolean? = null,
    colors: TextFieldColors = Utils.inputColors(),
    onValueChange: (String) -> Unit = {}
) {

    var value by rememberSaveable(field.uid) {
        mutableIntStateOf(field.counterValue())
    }
    val fieldValue = field.counterValue()

    LaunchedEffect(fieldValue) {
        if (value != fieldValue) {
            value = fieldValue
        }
    }

    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(0.dp, 300.dp)
            .then(modifier),
        value = value.toString(),
        onValueChange = {},
        label = { Text(text = field.label + if (field.mandatory == true) " *" else "") },
        enabled = (enable ?: field.enabled) == true,
        readOnly = true,
        singleLine = true,
        textStyle = LocalTextStyle.current.copy(
            textAlign = TextAlign.Center
        ),
        colors = colors,
        isError = field.hasError == true,
        supportingText = {
            FieldSupportingText(field)
        },
        shape = RoundedCornerShape(16.dp),
        leadingIcon = {
            IconButton(
                onClick = {
                    if (value > (field.minValue ?: 0)) {
                        value--
                        onValueChange(value.toString())
                    }
                },
                enabled = value > (field.minValue ?: 0),
                modifier = Modifier.background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(16.dp)
                ).padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrease",
                    tint = Color.White
                )
            }
        },
        trailingIcon = {
            IconButton(
                onClick = {
                    if (value < (field.maxValue ?: Int.MAX_VALUE)) {
                        value++
                        onValueChange(value.toString())
                    }
                },
                enabled = value < (field.maxValue ?: Int.MAX_VALUE),
                modifier = Modifier.background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(16.dp)
                ).padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase",
                    tint = Color.White
                )
            }
        }
    )

}

private fun FormFieldModel.counterValue(): Int {
    val min = minValue ?: 0
    val max = maxValue ?: Int.MAX_VALUE

    return (value?.toIntOrNull() ?: initialValue ?: min).coerceIn(min, max)
}
