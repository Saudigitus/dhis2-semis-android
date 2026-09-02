package org.saudigitus.campaign.core.designsystem.components.bottomsheets

import androidx.fragment.app.FragmentManager
import org.dhis2.commons.dialogs.bottomsheet.BottomSheetDialog
import org.dhis2.commons.dialogs.bottomsheet.BottomSheetDialogUiModel
import org.dhis2.commons.dialogs.bottomsheet.DialogButtonStyle
import org.saudigitus.campaign.core.designsystem.R

fun launchDiscardBottomSheet(
    title: String,
    subtitle: String,
    supportFragmentManager: FragmentManager,
    onDiscard: () -> Unit,
    onKeepEdition: () -> Unit,
) {
    BottomSheetDialog(
        bottomSheetDialogUiModel = BottomSheetDialogUiModel(
            title = title,
            message = subtitle,
            iconResource = R.drawable.ic_outline_error_36,
            mainButton = DialogButtonStyle.MainButton(org.dhis2.commons.R.string.keep_editing),
            secondaryButton = DialogButtonStyle.DiscardButton(),
        ),
        onMainButtonClicked = {
            supportFragmentManager.popBackStack()
            onKeepEdition.invoke()
        },
        onSecondaryButtonClicked = { onDiscard.invoke() },
        showTopDivider = true,
    ).apply {
        this.show(supportFragmentManager.beginTransaction(), "DIALOG")
        this.isCancelable = false
    }
}

fun launchDhis2BottomSheet(
    title: String,
    subtitle: String,
    iconResource: Int = org.dhis2.commons.R.drawable.ic_warning_alert,
    supportFragmentManager: FragmentManager,
    onCancel: () -> Unit,
    onSave: () -> Unit,
) {
    BottomSheetDialog(
        bottomSheetDialogUiModel = BottomSheetDialogUiModel(
            title = title,
            message = subtitle,
            iconResource = iconResource,
            mainButton = DialogButtonStyle.MainButton(org.dhis2.commons.R.string.save),
            secondaryButton = DialogButtonStyle.SecondaryButton(org.dhis2.commons.R.string.cancel),
        ),
        onMainButtonClicked = {
            supportFragmentManager.popBackStack()
            onSave.invoke()
        },
        onSecondaryButtonClicked = { onCancel.invoke() },
        showTopDivider = true,
    ).apply {
        this.show(supportFragmentManager.beginTransaction(), "DHIS2_DIALOG")
        this.isCancelable = false
    }
}