package org.saudigitus.campaign.core.data.utils.mapper

import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.SyncDisabled
import androidx.compose.material.icons.outlined.SyncProblem
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.dhis2.commons.data.EventModel
import org.dhis2.commons.date.toDateSpan
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.ui.model.ListCardUiModel
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItem
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItemColor
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.ProgressIndicator
import org.hisp.dhis.mobile.ui.designsystem.component.ProgressIndicatorType
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor
import org.saudigitus.campaign.core.data.R

class EventCardMapper(
    val context: Context,
    val resourceManager: ResourceManager,
) {
    fun map(
        event: EventModel,
        onCardClick: () -> Unit,
        onSyncIconClick: (String) -> Unit,
    ): ListCardUiModel =
        ListCardUiModel(
            title = context.getString(R.string.event_date, event.displayDate.orEmpty()),
            lastUpdated = event.lastUpdate.toDateSpan(context),
            additionalInfo = getAdditionalInfoList(event),
            actionButton = {
                ProvideSyncButton(
                    syncButtonLabel = resourceManager.getString(R.string.sync),
                    retryButtonLabel = resourceManager.getString(R.string.sync_retry),
                    state = event.event?.aggregatedSyncState(),
                    onSyncIconClick = { onSyncIconClick.invoke(event.event?.uid().orEmpty()) },
                )
            },
            expandLabelText = resourceManager.getString(R.string.show_more),
            shrinkLabelText = resourceManager.getString(R.string.show_less),
            onCardCLick = onCardClick,
        )

    private fun getAdditionalInfoList(
        event: EventModel,
    ): List<AdditionalInfoItem> {
        val list =
            event.dataElementValues
                ?.filter {
                    !it.second.isNullOrEmpty()
                }?.mapNotNull {
                    if (it.second != null) {
                        AdditionalInfoItem(
                            key = it.first,
                            value = it.second!!,
                        )
                    } else  null
                }?.toMutableList() ?: mutableListOf()

        checkRegisteredIn(
            list = list,
            orgUnit = event.orgUnitName,
        )

        checkSyncStatus(
            list = list,
            state = event.event?.aggregatedSyncState(),
        )

        return list
    }

    private fun checkRegisteredIn(
        list: MutableList<AdditionalInfoItem>,
        orgUnit: String,
    ) {
        list.add(
            AdditionalInfoItem(
                key = resourceManager.getString(R.string.administrative_area),
                value = orgUnit,
                isConstantItem = true,
            ),
        )
    }

    private fun checkSyncStatus(
        list: MutableList<AdditionalInfoItem>,
        state: State?,
    ) {
        val item =
            when (state) {
                State.TO_POST,
                State.TO_UPDATE,
                    -> {
                    AdditionalInfoItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.SyncDisabled,
                                contentDescription = resourceManager.getString(R.string.not_synced),
                                tint = AdditionalInfoItemColor.DISABLED.color,
                            )
                        },
                        value = resourceManager.getString(R.string.not_synced),
                        color = AdditionalInfoItemColor.DISABLED.color,
                        isConstantItem = true,
                    )
                }

                State.UPLOADING -> {
                    AdditionalInfoItem(
                        icon = {
                            ProgressIndicator(type = ProgressIndicatorType.CIRCULAR)
                        },
                        value = resourceManager.getString(R.string.syncing),
                        color = SurfaceColor.Primary,
                        isConstantItem = true,
                    )
                }

                State.ERROR -> {
                    AdditionalInfoItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.SyncProblem,
                                contentDescription = resourceManager.getString(R.string.sync_error_title),
                                tint = AdditionalInfoItemColor.ERROR.color,
                            )
                        },
                        value = resourceManager.getString(R.string.sync_error_title),
                        color = AdditionalInfoItemColor.ERROR.color,
                        isConstantItem = true,
                    )
                }

                State.WARNING -> {
                    AdditionalInfoItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.SyncProblem,
                                contentDescription = resourceManager.getString(R.string.sync_dialog_title_warning),
                                tint = AdditionalInfoItemColor.WARNING.color,
                            )
                        },
                        value = resourceManager.getString(R.string.sync_dialog_title_warning),
                        color = AdditionalInfoItemColor.WARNING.color,
                        isConstantItem = true,
                    )
                }

                else -> null
            }
        item?.let { list.add(it) }
    }
}

@Composable
fun ProvideSyncButton(
    syncButtonLabel: String,
    retryButtonLabel: String,
    state: State?,
    onSyncIconClick: () -> Unit,
) {
    val buttonText =
        when (state) {
            State.TO_POST,
            State.TO_UPDATE,
                -> {
                syncButtonLabel
            }

            State.ERROR,
            State.WARNING,
                -> {
                retryButtonLabel
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
            modifier = Modifier.fillMaxWidth(),
        )
    }
}