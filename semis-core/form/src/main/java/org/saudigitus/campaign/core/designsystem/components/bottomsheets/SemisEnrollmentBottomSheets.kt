package org.saudigitus.campaign.core.designsystem.components.bottomsheets

import androidx.fragment.app.FragmentManager
import org.dhis2.commons.dialogs.bottomsheet.BottomSheetDialog
import org.dhis2.commons.dialogs.bottomsheet.BottomSheetDialogUiModel
import org.dhis2.commons.dialogs.bottomsheet.DialogButtonStyle
import org.saudigitus.campaign.core.form.R

fun launchDiscardBottomSheet(title: String, subtitle: String, supportFragmentManager: FragmentManager, onDiscard: () -> Unit, onKeepEdition: () -> Unit) {
    BottomSheetDialog(BottomSheetDialogUiModel(
        title = title, message = subtitle, iconResource = org.dhis2.commons.R.drawable.ic_warning_alert,
        mainButton = DialogButtonStyle.MainButton(org.dhis2.commons.R.string.keep_editing),
        secondaryButton = DialogButtonStyle.DiscardButton()),
        onMainButtonClicked = { onKeepEdition() }, onSecondaryButtonClicked = { onDiscard() }, showTopDivider = true)
        .show(supportFragmentManager.beginTransaction(), "DISCARD_ENROLLMENT")
}

fun launchDhis2BottomSheet(title: String, subtitle: String, iconResource: Int = org.dhis2.commons.R.drawable.ic_warning_alert, supportFragmentManager: FragmentManager, onCancel: () -> Unit, onSave: () -> Unit) {
    BottomSheetDialog(BottomSheetDialogUiModel(
        title = title, message = subtitle, iconResource = iconResource,
        mainButton = DialogButtonStyle.MainButton(R.string.save),
        secondaryButton = DialogButtonStyle.SecondaryButton(org.dhis2.commons.R.string.cancel)),
        onMainButtonClicked = { onSave() }, onSecondaryButtonClicked = { onCancel() }, showTopDivider = true)
        .show(supportFragmentManager.beginTransaction(), "SAVE_ENROLLMENT")
}
