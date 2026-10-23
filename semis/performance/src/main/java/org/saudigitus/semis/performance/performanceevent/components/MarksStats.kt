package org.saudigitus.semis.performance.performanceevent.components

import org.saudigitus.semis.core.data.model.SearchTeiModel
import org.saudigitus.semis.core.form.data.model.FormFieldData
import java.util.Locale

/** Marks entered so far for the data element being captured. */
internal data class MarksStats(
    val total: Int = 0,
    val graded: Int = 0,
    val missing: Int = 0,
    val average: Double? = null,
    val highest: Double? = null,
)

internal fun marksStats(
    learners: List<SearchTeiModel>,
    dataElement: String?,
    fieldsData: List<FormFieldData>,
): MarksStats {
    // A mark shown in error is refused on save, so it counts as missing rather than pulling the
    // average and the highest mark towards a value that will never be kept.
    val values = learners.map { learner ->
        fieldsData.firstOrNull {
            it.tei == learner.tei.uid() && (dataElement == null || it.dataElement == dataElement)
        }?.takeUnless { it.hasError }?.value?.trim().orEmpty()
    }
    val entered = values.filter { it.isNotEmpty() }
    val numbers = entered.mapNotNull { it.replace(',', '.').toDoubleOrNull() }

    return MarksStats(
        total = learners.size,
        graded = entered.size,
        missing = learners.size - entered.size,
        average = numbers.average().takeIf { numbers.isNotEmpty() },
        highest = numbers.maxOrNull(),
    )
}

/** Marks read better without a trailing `.0`, so whole values are shown as integers. */
internal fun Double.formatMark(): String = if (this % 1.0 == 0.0) {
    toInt().toString()
} else {
    String.format(Locale.getDefault(), "%.1f", this)
}
