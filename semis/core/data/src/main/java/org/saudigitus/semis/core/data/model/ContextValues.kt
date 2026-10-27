package org.saudigitus.semis.core.data.model

/**
 * The context an event can actually carry, out of what it was offered.
 *
 * The values that say which class a record belongs to are whatever the configuration declares as
 * filters, and a program stage may hold data elements for all of them, for some, or for none. Only
 * what the stage holds is written, so a stage configured without them is left alone rather than
 * refused, and a stage that gains one later starts carrying it without a change here.
 *
 * @param stageDataElements the data elements the program stage is configured with
 * @param contextValues the configured context of the class, as data element to value
 */
fun contextValuesHeldBy(
    stageDataElements: Collection<String>,
    contextValues: List<Pair<String, String>>,
): List<Pair<String, String>> {
    if (stageDataElements.isEmpty() || contextValues.isEmpty()) return emptyList()

    val held = stageDataElements.toSet()

    return contextValues.filter { (dataElement, value) ->
        dataElement in held && dataElement.isNotBlank() && value.isNotBlank()
    }
}
