package org.saudigitus.semis.core.designsystem.utils.mapper

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import org.dhis2.commons.bindings.isFilePathValid
import org.dhis2.commons.date.toDateSpan
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.ui.model.ListCardUiModel
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItem
import org.hisp.dhis.mobile.ui.designsystem.component.Avatar
import org.hisp.dhis.mobile.ui.designsystem.component.AvatarStyleData
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.MetadataAvatarSize
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor
import org.saudigitus.semis.core.data.model.SearchTeiModel
import org.saudigitus.semis.core.designsystem.R
import java.io.File

class TEICardMapper(
    val context: Context,
    val resourceManager: ResourceManager,
) {

    fun map(
        searchTEIModel: SearchTeiModel,
        onSyncIconClick: () -> Unit,
        onCardClick: () -> Unit,
        onImageClick: (String) -> Unit,
        showSync: Boolean = false,
    ): ListCardUiModel {
        return ListCardUiModel(
            avatar = { ProvideAvatar(searchTEIModel, onImageClick) },
            title = getTitle(searchTEIModel),
            lastUpdated = searchTEIModel.tei.lastUpdated().toDateSpan(context),
            additionalInfo = getAdditionalInfoList(searchTEIModel),
            actionButton = {
                if (showSync) {
                    ProvideSyncButton(searchTEIModel, onSyncIconClick)
                }
            },
            expandLabelText = resourceManager.getString(R.string.show_more),
            shrinkLabelText = resourceManager.getString(R.string.show_less),
            onCardCLick = onCardClick,
        )
    }

    @Composable
    private fun ProvideAvatar(item: SearchTeiModel, onImageClick: ((String) -> Unit)) {
        val programUid: String? = if (item.selectedEnrollment != null) {
            item.selectedEnrollment.program().toString()
        } else {
            null
        }

        if (isFilePathValid(item.profilePicturePath)) {
            val file = File(item.profilePicturePath)
            val bitmap = BitmapFactory.decodeFile(file.absolutePath).asImageBitmap()
            val painter = BitmapPainter(bitmap)

            Avatar(
                style = AvatarStyleData.Image(painter),
                onImageClick = { onImageClick(item.profilePicturePath) },
            )
        } else if (item.isMetadataIconDataAvailable(programUid)) {
            Avatar(
                style = AvatarStyleData.Metadata(
                    imageCardData = item.getMetadataIconData(programUid).imageCardData,
                    avatarSize = MetadataAvatarSize.S(),
                    tintColor = item.getMetadataIconData(programUid).color,
                ),
            )
        } else {
            Avatar(
                style = AvatarStyleData.Text(getTitleFirstLetter(item)),
            )
        }
    }

    private fun getTitleFirstLetter(item: SearchTeiModel): String {
        val firstLetter = item.header?.firstOrNull()
            ?: item.attributeValues.values.firstOrNull()?.value()?.firstOrNull()

        return when (firstLetter) {
            null -> "?"
            '-' -> "?"
            else -> firstLetter.uppercaseChar().toString()
        }
    }

    private fun getTitle(item: SearchTeiModel): String {
        return item.header ?: run {
            val key = item.attributeValues.keys.firstOrNull() ?: "-"
            val value = item.attributeValues.values.firstOrNull()?.value() ?: "-"
            "$key: $value"
        }
    }

    private fun getAdditionalInfoList(searchTEIModel: SearchTeiModel): List<AdditionalInfoItem> {
        val attributeList = searchTEIModel.attributeValues.map {
            AdditionalInfoItem(
                key = it.key,
                value = it.value.value() ?: "-",
            )
        }.toMutableList()

        if (searchTEIModel.header == null) {
            attributeList.removeFirstOrNull()
        }
        attributeList.removeIf { it.value.isEmpty() || it.value == "-" }

        return if (attributeList.size > 2) {
            attributeList.subList(0,2)
        } else attributeList
    }

    @Composable
    private fun ProvideSyncButton(searchTEIModel: SearchTeiModel, onSyncIconClick: () -> Unit) {
        val buttonText = when (searchTEIModel.tei.aggregatedSyncState()) {
            State.TO_POST,
            State.TO_UPDATE,
                -> {
                resourceManager.getString(R.string.sync)
            }

            State.ERROR,
            State.WARNING,
                -> {
                resourceManager.getString(R.string.sync_retry)
            }

            else -> null
        }
        buttonText?.let {
            Button(
                style = ButtonStyle.TONAL,
                text = it,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Sync,
                        contentDescription = it,
                        tint = TextColor.OnPrimaryContainer,
                    )
                },
                onClick = { onSyncIconClick() },
                modifier = Modifier.Companion.fillMaxWidth(),
            )
        }
    }
}