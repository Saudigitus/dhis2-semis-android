package org.saudigitus.semis.core.designsystem.components.fields

import android.content.Context
import android.content.ContextWrapper
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
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.saudigitus.semis.core.data.model.OrgUnit
import org.saudigitus.semis.core.designsystem.theme.SemisFieldShape

/**
 * Which organisation units a field offers.
 *
 * [CAPTURE] is what a screen naming the user's own school needs: the places they record
 * into. [SEARCH] is for a field naming somebody else's school, such as where a record is
 * being sent, since a user seldom records into the school they are sending it to. It is
 * filled from the user's search organisation units, configured on the DHIS2 user.
 */
enum class OuFieldScope {
    CAPTURE,
    SEARCH,
    ;

    internal fun selectorScope(program: String): OrgUnitSelectorScope = when (this) {
        CAPTURE -> OrgUnitSelectorScope.ProgramCaptureScope(program)
        SEARCH -> OrgUnitSelectorScope.ProgramSearchScope(program)
    }
}

@Composable
fun OuField(
    modifier: Modifier = Modifier,
    placeholder: String,
    leadingIcon: ImageVector,
    selectedOrgUnit: OrgUnit? = null,
    program: String,
    enabled: Boolean = true,
    style: OuFieldStyle = OuFieldStyle.FILTER,
    scope: OuFieldScope = OuFieldScope.CAPTURE,
    label: String = placeholder,
    supportingText: String? = null,
    isError: Boolean = false,
    colors: TextFieldColors = TextFieldDefaults.colors(
        focusedContainerColor = SurfaceColor.Surface,
        unfocusedContainerColor = SurfaceColor.SurfaceDim,
        disabledContainerColor = SurfaceColor.DisabledSurface,
    ),
    onItemClick: (OrgUnit) -> Unit,
) {
    val fragmentManager = LocalContext.current
        .findFragmentActivity()
        ?.supportFragmentManager
    val selectionEnabled = enabled && fragmentManager != null

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    fun openSelector() {
        fragmentManager?.let {
            launchOuTreeSelector(
                supportFragmentManager = it,
                selectedOrgUnit = selectedOrgUnit,
                program = program,
                scope = scope,
                onOrgUnitSelected = onItemClick,
            )
        }
    }
    LaunchedEffect(isPressed) {
        if (selectionEnabled && isPressed) openSelector()
    }

    Column(
        modifier = if (style == OuFieldStyle.FORM) {
            modifier
        } else modifier.padding(horizontal = 16.dp)
    ) {
        val trailingIcon: @Composable () -> Unit = {
            IconButton(
                enabled = selectionEnabled,
                onClick = ::openSelector,
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
        if (style == OuFieldStyle.FORM) {
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = selectedOrgUnit?.displayName.orEmpty(),
                onValueChange = {},
                enabled = selectionEnabled,
                singleLine = true,
                readOnly = true,
                label = { Text(text = label) },
                placeholder = { Text(text = placeholder) },
                trailingIcon = trailingIcon,
                interactionSource = interactionSource,
                colors = colors,
                supportingText = { supportingText?.let { Text(text = it) } },
                isError = isError,
            )
        } else {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 2.dp,
                        ambientColor = Color.Black.copy(alpha = 0.1f),
                        shape = SemisFieldShape,
                        clip = false,
                    )
                    .background(color = Color.White, shape = SemisFieldShape),
                shape = SemisFieldShape,
                value = selectedOrgUnit?.displayName.orEmpty(),
                onValueChange = {},
                enabled = selectionEnabled,
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
                trailingIcon = trailingIcon,
                interactionSource = interactionSource,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White,
                ),
            )
        }
    }
}

private tailrec fun Context.findFragmentActivity(): FragmentActivity? = when (this) {
    is FragmentActivity -> this
    is ContextWrapper -> baseContext.findFragmentActivity()
    else -> null
}

private fun launchOuTreeSelector(
    supportFragmentManager: FragmentManager,
    selectedOrgUnit: OrgUnit? = null,
    program: String,
    scope: OuFieldScope,
    onOrgUnitSelected: (orgUnit: OrgUnit) -> Unit,
) {
    OUTreeFragment.Builder()
        .singleSelection()
        .orgUnitScope(scope.selectorScope(program))
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
