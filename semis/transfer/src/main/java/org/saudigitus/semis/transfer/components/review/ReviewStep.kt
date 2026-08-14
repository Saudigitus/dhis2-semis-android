package org.saudigitus.semis.transfer.components.review

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.saudigitus.semis.core.form.data.model.FormFieldState
import org.saudigitus.semis.core.form.ui.state.FormUiState
import org.saudigitus.semis.transfer.R
import org.saudigitus.semis.transfer.components.common.StepIntroduction
import org.saudigitus.semis.transfer.softShadow
import org.saudigitus.semis.transfer.state.TransferUiState
import java.text.DateFormat

@Composable
internal fun ReviewStep(state: TransferUiState, formState: FormUiState) {
    val rows = buildList {
        add(
            stringResource(R.string.learners) to
                stringResource(R.string.selected_count, state.selectedLearnerUids.size)
        )
        add(stringResource(R.string.from) to state.sourceOrgUnit?.displayName.orEmpty())
        formState.fields
            .filter {
                it.rendered &&
                    it.dataElementUid != state.originSchoolDataElement &&
                    !it.value.isNullOrBlank()
            }
            .forEach { field -> add(field.label to field.displayValue()) }
        add(
            stringResource(R.string.effective_date) to
                DateFormat.getDateInstance(DateFormat.MEDIUM).format(state.effectiveDate)
        )
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            StepIntroduction(
                title = stringResource(R.string.review),
                description = stringResource(R.string.transfer_review),
                horizontalPadding = 0.dp,
            )
        }
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .softShadow(MaterialTheme.shapes.medium, 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column(modifier = Modifier.padding(horizontal = 18.dp)) {
                    rows.forEachIndexed { index, row ->
                        ReviewRow(row.first, row.second, index < rows.lastIndex)
                    }
                }
            }
        }
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .softShadow(MaterialTheme.shapes.medium, 6.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.75f),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(
                    modifier = Modifier.padding(16.dp),
                    text = stringResource(R.string.transfer_warning),
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

private fun FormFieldState.displayValue(): String =
    selectedOrgUnit?.displayName
        ?: optionSet?.find { it.code == value }?.displayName
        ?: value.orEmpty()
