package org.saudigitus.semis.core.data.model.transfer

import org.saudigitus.semis.core.data.model.SearchTeiModel

data class LearnerIdentity(
    val name: String,
    val firstAttributeValue: String,
)

fun SearchTeiModel.learnerIdentity(): LearnerIdentity {
    val attributes = attributeValues.entries.mapNotNull { (label, attribute) ->
        attribute.value()?.takeIf(String::isNotBlank)?.let { label to it }
    }

    return learnerIdentity(attributes) {
        header?.takeUnless { it == tei.uid() }
            ?: attributes.firstOrNull()?.second?.takeIf(String::isNotBlank)
            ?: tei.uid()
    }
}

/**
 * Reads who the learner is from labelled values, wherever they come from.
 *
 * A learner is recognised by the same two things everywhere they are listed: the name, gathered
 * from whichever attributes are labelled as one, and the first value captured about them, which is
 * the number they are known by. Deriving it here rather than at each call site is what keeps a
 * learner looking the same on a listing and on the confirmation that they were enrolled.
 *
 * [fallbackName] is used only when nothing that reads as a name was captured.
 */
fun learnerIdentity(
    attributes: List<Pair<String, String>>,
    fallbackName: () -> String = { "" },
): LearnerIdentity {
    val firstAttributeValue = attributes.firstOrNull()?.second.orEmpty()
    val nameAttributes = attributes.filter { (label, _) -> label.isLearnerNameLabel() }
    val preferredFullName = nameAttributes.firstOrNull { (label, _) ->
        val words = label.normalizedLabel()
        "full" in words || "complete" in words ||
            ("student" in words && "name" in words) ||
            ("learner" in words && "name" in words)
    }?.second
    val name = preferredFullName
        ?: nameAttributes.map { it.second }
            .distinct()
            .joinToString(" ")
            .ifBlank { fallbackName() }

    return LearnerIdentity(
        name = name,
        firstAttributeValue = firstAttributeValue,
    )
}

private fun String.isLearnerNameLabel(): Boolean {
    val words = normalizedLabel()
    val nameWords = setOf("name", "surname", "nome", "apelido")
    val excludedWords = setOf(
        "school",
        "guardian",
        "parent",
        "mother",
        "father",
        "contact",
        "emergency",
    )
    return words.any { it in nameWords } && words.none { it in excludedWords }
}

private fun String.normalizedLabel(): List<String> = lowercase()
    .split(Regex("[^a-z0-9À-ÿ]+"))
    .filter(String::isNotBlank)
