package org.saudigitus.campaign.core.form.ui.fields

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing
import org.saudigitus.campaign.core.designsystem.theme.FormSurfaces
import org.saudigitus.campaign.core.designsystem.utils.Utils
import org.saudigitus.campaign.core.form.R
import org.saudigitus.campaign.core.form.data.models.FormFieldModel
import org.saudigitus.campaign.core.form.utils.phone.MozambiquePhoneTransformation
import org.saudigitus.campaign.core.form.utils.phone.MozambiquePhoneValidator

@Composable
fun PhoneNumberField(
    modifier: Modifier = Modifier,
    field: FormFieldModel,
    enable: Boolean? = null,
    onValueChange: (String) -> Unit,
    colors: TextFieldColors = Utils.inputColors(),
) {
    TextField(
        shape = FormSurfaces.FieldShape,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(Spacing.Spacing0, 300.dp)
            .then(modifier),
        enabled = (enable ?: field.enabled) == true,
        value = field.value.orEmpty(),
        onValueChange = {
            onValueChange(it.take(MozambiquePhoneValidator.MAX_LEN))
        },
        label = { Text(text = field.label + if (field.mandatory == true) " *" else "") },
        placeholder = { Text(text = field.label) },
        prefix = {
            Text(stringResource(R.string.phone_prefix))
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = field.label,
            )
        },
        isError = field.hasError == true,
        supportingText = {
            FieldSupportingText(field)
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Phone,
            imeAction = ImeAction.Done,
        ),
        colors = colors,
        visualTransformation = MozambiquePhoneTransformation(),
    )
}
