package org.saudigitus.semis.performance.performanceevent.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Save
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.saudigitus.semis.core.designsystem.components.buttons.SemisActionBar
import org.saudigitus.semis.performance.R

/**
 * Bottom action of the marks list: opens the form for edition and, once editing, saves the marks.
 */
@Composable
internal fun PerformanceSaveBar(
    isEditing: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    SemisActionBar(
        modifier = modifier,
        label = if (isEditing) {
            stringResource(R.string.performance_save_marks)
        } else {
            stringResource(R.string.performance_edit_marks)
        },
        icon = if (isEditing) Icons.Rounded.Save else Icons.Rounded.Edit,
        onClick = onClick,
    )
}
