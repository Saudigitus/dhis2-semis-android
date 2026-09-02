package org.saudigitus.campaign.core.form.ui.fields

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import org.saudigitus.campaign.core.data.models.OrgUnit
import org.saudigitus.campaign.core.designsystem.theme.FormSurfaces
import org.saudigitus.campaign.core.designsystem.utils.Utils
import org.saudigitus.campaign.core.form.data.models.FormFieldModel

/** Read-only organisation-unit controls for the SEMIS enrollment form. */
@Composable
fun OuField(
    modifier: Modifier = Modifier,
    placeholder: String,
    leadingIcon: ImageVector,
    selectedOrgUnit: OrgUnit? = null,
    program: String,
    onItemClick: (OrgUnit) -> Unit,
) {
    TextField(
        shape = FormSurfaces.FieldShape,
        modifier = Modifier.fillMaxWidth().then(modifier),
        value = selectedOrgUnit?.displayName.orEmpty(),
        onValueChange = {},
        readOnly = true,
        label = { Text(placeholder) },
        leadingIcon = { Icon(leadingIcon, null) },
        colors = Utils.inputColors(),
    )
}

@Composable
fun OuField(
    modifier: Modifier = Modifier,
    field: FormFieldModel,
    onValueChange: (String) -> Unit,
) {
    TextField(
        shape = FormSurfaces.FieldShape,
        modifier = Modifier.fillMaxWidth().then(modifier),
        value = field.value.orEmpty(),
        onValueChange = onValueChange,
        label = { Text(field.label) },
        colors = Utils.inputColors(),
    )
}
