package org.saudigitus.semis.core.data.model.person

import org.hisp.dhis.android.core.common.ValueType
import org.saudigitus.semis.core.data.model.SearchTeiModel

/**
 * One attribute of a person, carrying what the identity rule reads from the metadata.
 *
 * [value] is what the reader sees: an option name rather than its code, a school name rather than
 * its uid. [tag] is set only when the attribute has an option set whose options all have a colour,
 * because only then is the value meant to be shown as a coloured tag.
 */
data class PersonAttribute(
    val uid: String,
    val label: String,
    val value: String,
    val valueType: ValueType?,
    val unique: Boolean,
    val displayInList: Boolean,
    val hasOptionSet: Boolean,
    val tag: PersonTag? = null,
)

/**
 * An option shown next to the name, the way the web shows it: in the option colour, or plain when
 * the option has none.
 */
data class PersonTag(
    val label: String,
    val color: String?,
)

/**
 * Who a person is, read the same way on every list, card and profile, and the same way the web
 * reads it.
 *
 * [name] is blank when nothing at all was captured; the screen then says so in the user's
 * language. It is never the record id. [usedAttributes] are the attributes the name and the
 * identifier were taken from, so a card can list the other attributes without repeating them.
 */
data class PersonIdentity(
    val name: String,
    val identifier: String,
    val photoPath: String? = null,
    val tags: List<PersonTag> = emptyList(),
    val usedAttributes: Set<String> = emptySet(),
)

/**
 * Applies the shared rule to the attributes of a person, given in program order.
 *
 * - Name: the text attributes marked "display in list" that are neither unique nor backed by an
 *   option set, joined by a space. Without any, the first value shown in the list, then the
 *   identifier.
 * - Identifier: the first unique attribute marked "display in list", otherwise the first unique
 *   attribute.
 * - Photo: the first image attribute.
 * - Tags: every attribute whose options all have a colour.
 *
 * Nothing here reads the label text, which is what keeps the rule working in any language and
 * with any wording the metadata uses.
 */
fun personIdentity(attributes: List<PersonAttribute>): PersonIdentity {
    val captured = attributes.filter { it.value.isNotBlank() }
    val nameParts = captured.filter { attribute ->
        attribute.displayInList &&
            !attribute.unique &&
            !attribute.hasOptionSet &&
            attribute.valueType in NAME_VALUE_TYPES
    }
    val identifier = captured.firstOrNull { it.unique && it.displayInList }
        ?: captured.firstOrNull { it.unique }

    val name = nameParts.joinToString(" ") { it.value.trim() }
        .ifBlank { captured.firstOrNull { it.displayInList }?.value.orEmpty() }
        .ifBlank { identifier?.value.orEmpty() }

    return PersonIdentity(
        name = name,
        identifier = identifier?.value.orEmpty(),
        photoPath = captured.firstOrNull { it.valueType == ValueType.IMAGE }?.value,
        tags = captured.mapNotNull { it.tag },
        usedAttributes = (nameParts.map { it.uid } + listOfNotNull(identifier?.uid)).toSet(),
    )
}

private val NAME_VALUE_TYPES = setOf(ValueType.TEXT, ValueType.LONG_TEXT)

/**
 * Who this listed person is. Every person that went through the transformation carries the
 * identity read with the shared rule; a model built some other way only has its first value.
 */
fun SearchTeiModel.identity(): PersonIdentity =
    personIdentity ?: PersonIdentity(
        name = attributeValues.values.firstNotNullOfOrNull { it.value()?.takeIf(String::isNotBlank) }
            .orEmpty(),
        identifier = "",
    )
