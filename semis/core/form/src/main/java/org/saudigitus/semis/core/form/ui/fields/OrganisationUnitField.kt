package org.saudigitus.semis.core.form.ui.fields

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.saudigitus.semis.core.data.model.OrgUnit
import org.saudigitus.semis.core.designsystem.components.fields.OuField
import org.saudigitus.semis.core.designsystem.components.fields.OuFieldStyle
import org.saudigitus.semis.core.form.data.model.FormFieldState

@Composable
fun OrganisationUnitField(
    field: FormFieldState,
    program: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: TextFieldColors = TextFieldDefaults.colors(),
    onValueChange: (OrgUnit) -> Unit,
) {
    OuField(
        modifier = modifier,
        placeholder = field.label,
        leadingIcon = Icons.Outlined.School,
        selectedOrgUnit = field.selectedOrgUnit,
        program = program,
        enabled = enabled,
        style = OuFieldStyle.FORM,
        label = field.label + if (field.mandatory) " *" else "",
        supportingText = field.errorMessage,
        isError = field.hasError,
        colors = colors,
        onItemClick = onValueChange,
    )
}
