package org.saudigitus.campaign.core.form.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.hisp.dhis.android.core.common.ValueType
import org.saudigitus.campaign.core.form.data.models.FormFieldModel
import org.saudigitus.campaign.core.form.ui.fields.CoordinateField
import org.saudigitus.campaign.core.form.ui.fields.CounterTextField
import org.saudigitus.campaign.core.form.ui.fields.DateField
import org.saudigitus.campaign.core.form.ui.fields.InputField
import org.saudigitus.campaign.core.form.ui.fields.NumericField
import org.saudigitus.campaign.core.form.ui.fields.OptionSetField
import org.saudigitus.campaign.core.form.ui.fields.OuField
import org.saudigitus.campaign.core.form.ui.fields.PhoneNumberField
import org.saudigitus.campaign.core.form.ui.fields.QRField
import org.saudigitus.campaign.core.form.ui.fields.ScannableDropdownField
import org.saudigitus.campaign.core.form.ui.fields.SearchableDropdown
import org.saudigitus.campaign.core.form.ui.fields.SearchableOrgUnitDropdown
import org.saudigitus.campaign.core.form.ui.fields.TrueOnlyField
import org.saudigitus.campaign.core.form.ui.fields.YesNoField
import org.saudigitus.campaign.core.form.utils.CustomValueType
import org.saudigitus.campaign.core.form.utils.FormValueType
import org.saudigitus.campaign.core.form.utils.Utils
import org.saudigitus.campaign.core.utils.location.state.CoordinateState

@Composable
fun FormFieldItem(
    modifier: Modifier = Modifier,
    field: FormFieldModel,
    enabled: Boolean? = null,
    colors: TextFieldColors = Utils.inputColors(),
    coordinateState: CoordinateState? = null,
    onQuery: (field: FormFieldModel, query: String) -> Unit = { _, _ -> },
    onValueChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when {
            field.valueType == ValueType.BOOLEAN && field.rendered == true -> YesNoField(
                field = field,
                enabled = enabled ?: field.enabled ?: true,
                onValueChange = onValueChange
            )

            !field.optionSet.isNullOrEmpty()
                && field.customValueType != CustomValueType.SCANNABLE_DROPDOWN_FIELD
                && field.rendered == true -> {
                OptionSetField(
                    field,
                    enabled = enabled,
                    colors = colors,
                    onValueChange
                )
            }

            field.valueType == ValueType.TRUE_ONLY && field.rendered == true -> TrueOnlyField(
                modifier,
                field,
                enabled,
                onValueChange
            )

            field.valueType in FormValueType.integer && field.rendered == true -> NumericField(
                modifier,
                field,
                enabled = enabled,
                colors,
                onValueChange
            )

            field.valueType in FormValueType.text && field.rendered == true -> InputField(
                modifier = modifier,
                field = field,
                enable = enabled,
                colors = colors,
                onValueChange = onValueChange
            )

            field.valueType == ValueType.COORDINATE && field.rendered == true -> CoordinateField(
                modifier = modifier,
                field = field,
                coordinateState = coordinateState,
                colors = colors,
            )

            field.valueType == ValueType.PHONE_NUMBER && field.rendered == true -> PhoneNumberField(
                modifier = modifier,
                field = field,
                colors = colors,
                onValueChange = onValueChange
            )

            field.valueType in FormValueType.date && field.rendered == true -> DateField(
                modifier = modifier,
                field = field,
                enable = enabled,
                colors = colors,
                onValueChange = onValueChange
            )

            (field.valueType == ValueType.ORGANISATION_UNIT
                || field.customValueType == CustomValueType.ORG_UNIT) && field.rendered == true -> OuField(
                modifier = modifier,
                field = field,
                onValueChange
            )

            field.customValueType == CustomValueType.COUNTER && field.rendered == true -> CounterTextField(
                modifier = modifier,
                field = field,
                colors = colors,
                onValueChange = onValueChange
            )

            field.customValueType == CustomValueType.SEARCHABLE_FIELD && field.rendered == true -> SearchableDropdown(
                modifier = modifier,
                field = field,
                colors = colors,
                onQuery = onQuery,
                onItemClick = onValueChange,
            )

            field.customValueType == CustomValueType.SEARCHABLE_ORG_UNIT_FIELD && field.rendered == true -> SearchableOrgUnitDropdown(
                modifier = modifier,
                field = field,
                colors = colors,
                onQuery = onQuery,
                onItemClick = onValueChange,
            )

            field.customValueType == CustomValueType.QR_FIELD && field.rendered == true -> QRField(
                modifier = modifier,
                field = field,
                enable = enabled,
                colors = colors,
                onValueChange = onValueChange
            )

            field.customValueType == CustomValueType.SCANNABLE_DROPDOWN_FIELD && field.rendered == true -> ScannableDropdownField(
                field,
                enabled = enabled,
                colors = colors,
                onValueChange
            )
        }
    }
}
