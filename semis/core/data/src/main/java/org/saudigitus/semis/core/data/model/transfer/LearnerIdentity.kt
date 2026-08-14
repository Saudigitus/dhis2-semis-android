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
            .ifBlank {
                header?.takeUnless { it == tei.uid() }
                    ?: firstAttributeValue.takeIf(String::isNotBlank)
                    ?: tei.uid()
            }

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
