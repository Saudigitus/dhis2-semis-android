package org.saudigitus.campaign.core.form.ui.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.saudigitus.campaign.core.designsystem.theme.FormSurfaces
import org.saudigitus.campaign.core.designsystem.theme.formSoftShadow
import org.saudigitus.campaign.core.form.data.models.FormSectionModel
import org.saudigitus.campaign.core.form.ui.FormFieldItem
import org.saudigitus.campaign.core.form.ui.component.MandatoryFieldWrapper
import org.saudigitus.campaign.core.form.ui.state.FormEvent
import org.saudigitus.campaign.core.utils.location.state.CoordinateState

private val SectionShape = RoundedCornerShape(20.dp)

/**
 * A form section laid out as a card: its name and description on top and every rendered field
 * underneath, so the whole form reads as a single scroll instead of one page per section.
 */
@Composable
internal fun FormSectionCard(
    section: FormSectionModel,
    showMandatoryError: Boolean,
    coordinateStates: Map<String, CoordinateState>,
    modifier: Modifier = Modifier,
    onEvent: (FormEvent) -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .formSoftShadow(SectionShape),
        shape = SectionShape,
        colors = CardDefaults.cardColors(containerColor = FormSurfaces.CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            section.name?.takeIf { it.isNotBlank() }?.let { name ->
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = name.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = FormSurfaces.SectionTitle,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                )
            }

            section.description?.takeIf { it.isNotBlank() }?.let { description ->
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = FormSurfaces.TextSecondary,
                )
            }

            section.formFields.filter { it.rendered == true }.forEach { field ->
                val fieldItem: @Composable () -> Unit = {
                    FormFieldItem(
                        modifier = Modifier.fillMaxWidth(),
                        field = field,
                        enabled = field.enabled,
                        coordinateState = coordinateStates[field.uid],
                        onValueChange = { value ->
                            onEvent(FormEvent.UpdateField(section, field.uid, value))
                        },
                        onQuery = { fieldModel, query ->
                            onEvent(FormEvent.SearchFieldQuery(section, fieldModel.uid, query))
                        },
                    )
                }

                if (field.mandatory == true) {
                    MandatoryFieldWrapper(showMandatoryError) { fieldItem() }
                } else {
                    fieldItem()
                }
            }
        }
    }
}
