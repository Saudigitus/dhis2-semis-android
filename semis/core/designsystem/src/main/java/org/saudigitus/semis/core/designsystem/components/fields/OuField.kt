package org.saudigitus.semis.core.designsystem.components.fields

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import org.dhis2.commons.orgunitselector.OUTreeFragment
import org.dhis2.commons.orgunitselector.OrgUnitSelectorScope
import org.dhis2.ui.theme.colorPrimary
import org.saudigitus.semis.core.data.model.OrgUnit

@Composable
fun OuField(
    modifier: Modifier = Modifier,
    placeholder: String,
    leadingIcon: ImageVector,
    selectedOrgUnit: OrgUnit? = null,
    program: String,
    onItemClick: (OrgUnit) -> Unit,
) {
    val context = LocalContext.current
    val fragmentManager = (context as? FragmentActivity)?.supportFragmentManager

    val interactionSource = remember { MutableInteractionSource() }
    if (interactionSource.collectIsPressedAsState().value) {
        launchOuTreeSelector(
            supportFragmentManager = fragmentManager!!,
            selectedOrgUnit = selectedOrgUnit,
            program = program,
            onOrgUnitSelected = {
                onItemClick.invoke(it)
            },
        )
    }

    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 2.dp,
                    ambientColor = Color.Black.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(30.dp),
                    clip = false,
                )
                .background(color = Color.White, shape = RoundedCornerShape(30.dp)),
            shape = RoundedCornerShape(30.dp),
            value = selectedOrgUnit?.displayName ?: "",
            onValueChange = {},
            singleLine = true,
            readOnly = true,
            placeholder = { Text(text = placeholder) },
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = colorPrimary,
                )
            },
            trailingIcon = {
                IconButton(onClick = {
                    launchOuTreeSelector(
                        supportFragmentManager = fragmentManager!!,
                        selectedOrgUnit = selectedOrgUnit,
                        program = program,
                        onOrgUnitSelected = {
                            onItemClick.invoke(it)
                        },
                    )
                }) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            },
            interactionSource = interactionSource,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White,
            ),
        )
    }
}

private fun launchOuTreeSelector(
    supportFragmentManager: FragmentManager,
    selectedOrgUnit: OrgUnit? = null,
    program: String,
    onOrgUnitSelected: (orgUnit: OrgUnit) -> Unit,
) {
    OUTreeFragment.Builder()
        .singleSelection()
        .orgUnitScope(OrgUnitSelectorScope.ProgramCaptureScope(program))
        .withPreselectedOrgUnits(
            selectedOrgUnit?.let { listOf(it.uid) } ?: emptyList(),
        )
        .onSelection { selectedOrgUnits ->
            val selectedOrgUnit = selectedOrgUnits.firstOrNull()
            if (selectedOrgUnit != null) {
                onOrgUnitSelected(
                    OrgUnit(
                        uid = selectedOrgUnit.uid(),
                        displayName = selectedOrgUnit.displayName(),
                    ),
                )
            }
        }
        .build()
        .show(supportFragmentManager, "OU_TREE")
}