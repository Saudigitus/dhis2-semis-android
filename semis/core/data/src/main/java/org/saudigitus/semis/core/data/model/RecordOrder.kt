package org.saudigitus.semis.core.data.model

/**
 * The order a deployment wants its records listed in, as the datastore states it per program.
 *
 * The setting is written as `<attribute uid>:<asc|desc>`, for example `gz8w04YBSS0:asc`. It names
 * an attribute of whatever the program tracks, so the same setting orders students, staff and
 * anything a future program registers. Nothing here knows what kind of record it is putting in
 * order, which is what lets one rule serve them all.
 */
internal data class RecordOrder(
    val attribute: String,
    val descending: Boolean,
)

/**
 * Reads the configured order, or nothing when the deployment did not state one.
 *
 * Anything that is not a uid followed by a direction is treated as unstated rather than as an
 * error: a list in the order the records arrived is a worse list, but a working one, and a
 * malformed setting must not be what empties a listing.
 */
internal fun recordOrderOf(defaultOrder: String?): RecordOrder? {
    val parts = defaultOrder?.split(":") ?: return null
    val attribute = parts.firstOrNull()?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    val direction = parts.getOrNull(1)?.trim()?.lowercase()

    if (parts.size > 2) return null
    if (direction != null && direction != ASCENDING && direction != DESCENDING) return null

    return RecordOrder(attribute = attribute, descending = direction == DESCENDING)
}

/**
 * Puts the records in the configured order, reading the value to compare through [valueOf].
 *
 * A record with no value for the attribute is placed last whichever direction is asked for,
 * because what the reader is looking for is the named ones and a blank at the top of the list only
 * gets in the way. Comparison ignores case, so a listing does not split into two alphabets, and
 * the order the records came in is kept when none is configured.
 */
internal fun <T> List<T>.orderedBy(
    order: RecordOrder?,
    valueOf: (T) -> String?,
): List<T> {
    if (order == null) return this

    val blankLast = compareBy<T> { valueOf(it).isNullOrBlank() }
    val byValue = if (order.descending) {
        blankLast.thenByDescending(String.CASE_INSENSITIVE_ORDER) { valueOf(it).orEmpty() }
    } else {
        blankLast.thenBy(String.CASE_INSENSITIVE_ORDER) { valueOf(it).orEmpty() }
    }

    return sortedWith(byValue)
}

private const val ASCENDING = "asc"
private const val DESCENDING = "desc"
