package org.saudigitus.campaign.core.form.ui.fields

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing
import org.saudigitus.campaign.core.designsystem.theme.FormSurfaces
import org.saudigitus.campaign.core.designsystem.theme.dark_warning
import org.saudigitus.campaign.core.designsystem.theme.light_green
import org.saudigitus.campaign.core.designsystem.theme.light_success
import org.saudigitus.campaign.core.designsystem.theme.light_warning
import org.saudigitus.campaign.core.designsystem.utils.Utils
import org.saudigitus.campaign.core.form.R
import org.saudigitus.campaign.core.form.data.models.FormFieldModel
import org.saudigitus.campaign.core.utils.location.state.CoordinateState

private const val TARGET_ACCURACY_METERS = 5f

@Composable
fun CoordinateField(
    modifier: Modifier = Modifier,
    field: FormFieldModel,
    enable: Boolean? = null,
    coordinateState: CoordinateState? = null,
    colors: TextFieldColors = Utils.inputColors(),
) {
    val isEnabled = (enable ?: field.enabled) == true
    val hasGoodAccuracy = coordinateState?.accuracy
        ?.let { it <= TARGET_ACCURACY_METERS } == true
    val accuracyText = coordinateState?.accuracy?.toString() ?: "-"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        TextField(
            shape = FormSurfaces.FieldShape,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(Spacing.Spacing0, 300.dp)
                .then(modifier),
            value = field.getFormatedCoordinates().orEmpty(),
            onValueChange = {},
            label = { Text(text = field.label + if (field.mandatory == true) " *" else "") },
            placeholder = { Text(text = field.label) },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.location),
                    contentDescription = field.label,
                )
            },
            enabled = isEnabled,
            isError = field.hasError == true,
            supportingText = {
                FieldSupportingText(field)
            },
            readOnly = true,
            singleLine = true,
            colors = colors,
        )
        SuggestionChip(
            modifier = Modifier.offset(0.dp, (-18).dp),
            onClick = { },
            label = {
                Text(
                    stringResource(
                        R.string.current_precision,
                        accuracyText
                    )
                )
            },
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(0.dp, Color.Transparent),
            colors = SuggestionChipDefaults.suggestionChipColors(
                containerColor = if (hasGoodAccuracy) {
                    light_success.copy(0.25f)
                } else {
                    light_warning.copy(0.25f)
                },
                labelColor = if (hasGoodAccuracy) light_green else dark_warning
            )
        )
    }
}
