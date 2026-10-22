package org.saudigitus.semis.core.data.utils

import org.dhis2.bindings.userFriendlyValue
import org.dhis2.commons.resources.ResourceManager
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.ValueType
import org.saudigitus.semis.core.data.R
import javax.inject.Inject

/**
 * Turns a stored value into what the reader should see, the same way on every SEMIS screen.
 *
 * The DHIS2 formatter already gives option names instead of codes, school names instead of uids
 * and dates in the user's format. Yes and no answers are the one thing it leaves as stored, so
 * they are worded here, in the user's language.
 */
class ValueDisplay @Inject constructor(
    private val d2: D2,
    private val resourceManager: ResourceManager,
) {

    fun of(raw: String?, valueType: ValueType?, optionSetUid: String?): String? {
        val value = raw?.takeIf { it.isNotBlank() } ?: return null

        return when (valueType) {
            ValueType.BOOLEAN, ValueType.TRUE_ONLY -> when (value.lowercase()) {
                "true" -> resourceManager.getString(R.string.value_yes)
                "false" -> resourceManager.getString(R.string.value_no)
                else -> value
            }

            else -> value.userFriendlyValue(d2, valueType, optionSetUid) ?: value
        }
    }
}
