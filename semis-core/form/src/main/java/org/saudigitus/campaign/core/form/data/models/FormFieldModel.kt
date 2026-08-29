package org.saudigitus.campaign.core.form.data.models

import org.dhis2.commons.orgunitselector.OUTreeModel
import org.hisp.dhis.android.core.common.ValueType
import org.saudigitus.campaign.core.data.models.OptionModel
import org.saudigitus.campaign.core.data.models.OrgUnit
import org.saudigitus.campaign.core.data.models.OuHideStrategy
import org.saudigitus.campaign.core.form.utils.CustomValueType


data class FormFieldModel(
    val uid: String,
    val label: String,
    val valueType: ValueType? = ValueType.TEXT,
    val customValueType: CustomValueType? = null,
    val value: String? = null,
    val initialValue: Int? = -1,
    val minValue: Int? = 0,
    val maxValue: Int? = Int.MAX_VALUE,
    val dlToLimit: String? = null,
    val optionSet: List<OptionModel>? = null,
    val orgUnits: List<OrgUnit>? = null,
    val ouTreeModel: OUTreeModel? = null,
    val mandatory: Boolean? = false,
    val baseMandatory: Boolean? = mandatory,
    val rendered: Boolean? = true,
    val enabled: Boolean? = true,
    /**
     * The server minted this value from a pattern rather than the user typing it, which is what
     * lets the identifiers be shown back once the record is written.
     */
    val generated: Boolean = false,
    val enabledOnAssign: Boolean? = null,
    val hasError: Boolean? = false,
    val errorMessage: String? = null,
    val hasWarning: Boolean? = false,
    val warningMessage: String? = null,
    val ouHideStrategy: OuHideStrategy? = null,
    val canSelectOUParent: Boolean = true,
    val userId: String? = null,
    val userLevel: Int? = null,
) {
    val hasSupportingMessages: Boolean
        get() = !errorMessage.isNullOrBlank() ||
            (hasWarning == true && !warningMessage.isNullOrBlank())

    fun hasValidCoordinates(): Boolean {
        if (valueType != ValueType.COORDINATE) return false

        val coordinates = parseCoordinates() ?: return false
        val longitude = coordinates.first.toDoubleOrNull() ?: return false
        val latitude = coordinates.second.toDoubleOrNull() ?: return false

        return longitude != 0.0 || latitude != 0.0
    }

    fun getFormatedCoordinates(): String? {
        return if (valueType == ValueType.COORDINATE) {
            if (!hasValidCoordinates()) return null

            val coordinates = parseCoordinates() ?: return null
            val longitude = coordinates.first
            val latitude = coordinates.second

            "Lat: $latitude, Long: $longitude"
        } else null
    }

    private fun parseCoordinates(): Pair<String, String>? {
        if (value.isNullOrBlank()) return null

        val cleaned = value
            .removePrefix("[")
            .removeSuffix("]")
            .split(",")

        if (cleaned.size != 2) return null

        return cleaned[0].trim() to cleaned[1].trim()
    }

    override fun toString(): String {
        return "FormFieldModel(uid='$uid', label='$label', valueType=$valueType, customValue=$customValueType, value=$value, rendered=$rendered)"
    }
}
