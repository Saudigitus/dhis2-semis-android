package org.saudigitus.semis.core.data.utils

import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.ValueType

/**
 * One data element of a program stage, with what a screen needs to show it.
 *
 * [displayInReports] is the stage's "display in reports" flag, which the web reads as "show this
 * column", so the app shows a value of a stage exactly when the web does.
 */
data class StageDataElement(
    val uid: String,
    val label: String,
    val valueType: ValueType?,
    val optionSet: String?,
    val displayInReports: Boolean,
)

/** The data elements of [stage] in stage order. */
fun D2.stageDataElements(stage: String): List<StageDataElement> =
    programModule().programStageDataElements()
        .byProgramStage().eq(stage)
        .blockingGet()
        .sortedBy { it.sortOrder() ?: Int.MAX_VALUE }
        .mapNotNull { stageElement ->
            val uid = stageElement.dataElement()?.uid() ?: return@mapNotNull null
            val element = dataElementModule().dataElements().uid(uid).blockingGet()
                ?: return@mapNotNull null

            StageDataElement(
                uid = uid,
                label = element.displayFormName()?.takeIf { it.isNotBlank() }
                    ?: element.displayName().orEmpty(),
                valueType = element.valueType(),
                optionSet = element.optionSetUid(),
                displayInReports = stageElement.displayInReports() == true,
            )
        }
