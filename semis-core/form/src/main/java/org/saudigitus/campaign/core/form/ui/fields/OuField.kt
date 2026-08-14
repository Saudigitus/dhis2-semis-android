package org.saudigitus.campaign.core.form.ui.fields

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import org.dhis2.commons.orgunitselector.OUTreeFragment
import org.dhis2.commons.orgunitselector.OUTreeModel
import org.dhis2.mobile.commons.orgunit.OrgUnitSelectorScope
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.saudigitus.campaign.core.data.models.OrgUnit
import org.saudigitus.campaign.core.designsystem.utils.Utils
import org.saudigitus.campaign.core.form.R
import org.saudigitus.campaign.core.form.data.models.FormFieldModel

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
            selectedOrgUnit = selectedOrgUnit?.uid,
            program = program,
            onOrgUnitSelected = {
                onItemClick.invoke(it)
            },
        )
    }

    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier),
        value = selectedOrgUnit?.displayName.orEmpty(),
        onValueChange = {},
        singleLine = true,
        readOnly = true,
        enabled = false,
        placeholder = { Text(text = placeholder) },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = SurfaceColor.Primary,
            )
        },
        trailingIcon = {
            IconButton(
                onClick = {
                    launchOuTreeSelector(
                        supportFragmentManager = fragmentManager!!,
                        selectedOrgUnit = selectedOrgUnit?.uid,
                        program = program,
                        onOrgUnitSelected = {
                            onItemClick.invoke(it)
                        },
                    )
                }, enabled = false
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        },
        interactionSource = interactionSource,
        colors = Utils.inputColors(),
    )
}

@Composable
fun OuField(
    modifier: Modifier = Modifier,
    field: FormFieldModel,
    onValueChange: (String) -> Unit,
) {
    val context = LocalContext.current
    val fragmentManager = (context as? FragmentActivity)?.supportFragmentManager

    var orgUnit by remember { mutableStateOf("") }

    val interactionSource = remember { MutableInteractionSource() }
    if (interactionSource.collectIsPressedAsState().value) {
        launchOuTreeFieldSelector(
            supportFragmentManager = fragmentManager!!,
            selectedOrgUnit = field.value,
            userId = field.userId.orEmpty(),
            onOrgUnitSelected = {
                orgUnit = it.displayName.orEmpty()
                onValueChange.invoke(it.uid)
            },
        )
    }

    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier),
        value = orgUnit,
        onValueChange = onValueChange,
        singleLine = true,
        readOnly = true,
        label = { Text(text = field.label + if (field.mandatory == true) " *" else "") },
        placeholder = { Text(text = field.label) },
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.org_unit),
                contentDescription = field.label,
                tint = SurfaceColor.Primary,
            )
        },
        trailingIcon = {
            IconButton(onClick = {
                launchOuTreeFieldSelector(
                    supportFragmentManager = fragmentManager!!,
                    selectedOrgUnit = field.value,
                    ouTreeModel = field.ouTreeModel,
                    userId = field.userId.orEmpty(),
                    onOrgUnitSelected = {
                        orgUnit = it.displayName.orEmpty()
                        onValueChange.invoke(it.uid)
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
        colors = Utils.inputColors(),
    )
}

fun launchOuTreeSelector(
    supportFragmentManager: FragmentManager,
    selectedOrgUnit: String? = null,
    program: String,
    onOrgUnitSelected: (orgUnit: OrgUnit) -> Unit,
) {
    OUTreeFragment.Builder()
        .singleSelection()
        .orgUnitScope(OrgUnitSelectorScope.ProgramCaptureScope(program))
        .withPreselectedOrgUnits(
            selectedOrgUnit?.let { listOf(it) } ?: emptyList(),
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

fun launchOuTreeFieldSelector(
    supportFragmentManager: FragmentManager,
    selectedOrgUnit: String? = null,
    ouTreeModel: OUTreeModel? = null,
    userId: String,
    onOrgUnitSelected: (orgUnit: OrgUnit) -> Unit,
) {
    var ouTree = OUTreeFragment.Builder()
        .singleSelection()
        .orgUnitScope(OrgUnitSelectorScope.UserSearchScope(userId))
        .withPreselectedOrgUnits(
            selectedOrgUnit?.let { listOf(it) } ?: emptyList(),
        )

    ouTree = if (ouTreeModel != null) {
        ouTree.withModel(ouTreeModel)
    } else ouTree

    ouTree
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