package org.saudigitus.semis.core.designsystem.components.bottomsheet


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.saudigitus.semis.core.designsystem.R
import org.saudigitus.semis.core.designsystem.components.bottomsheet.model.BottomSheetState
import org.saudigitus.semis.core.designsystem.components.cards.SummaryCard
import org.saudigitus.semis.core.designsystem.utils.UiDefaults


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryBottomSheet(
    state: BottomSheetState.SaveState,
    onDismissRequest: () -> Unit,
    onSave: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(460.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = state.title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = state.title,
                    textAlign = TextAlign.Center,
                    fontSize = MaterialTheme.typography.titleMedium.fontSize
                )
            }
            HorizontalDivider(thickness = 0.75.dp)
            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                LazyColumn(
                    modifier = Modifier.wrapContentSize(),
                    verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.Top),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(state.indicators) { indicator ->
                        SummaryCard(
                            modifier = Modifier.fillMaxWidth(),
                            icon = indicator.icon
                                ?: ImageVector.vectorResource(UiDefaults.getIconByName(indicator.iconName.orEmpty())),
                            title = indicator.label.orEmpty(),
                            subtitle = indicator.value.orEmpty(),
                            colors = CardDefaults.cardColors(
                                containerColor = indicator.color ?: Color.LightGray,
                                contentColor = Color.White
                            )
                        )
                    }
                }
            }
            HorizontalDivider(thickness = 0.75.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onDismissRequest,
                ) {
                    Text(text = stringResource(R.string.cancel))
                }

                Button(
                    modifier = Modifier.weight(1f),
                    onClick = onSave,
                ) {
                    Text(text = stringResource(R.string.save))
                }
            }
        }
    }
}