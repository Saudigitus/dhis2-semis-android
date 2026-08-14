package org.saudigitus.campaign.core.form.utils.phone

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class MozambiquePhoneTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val cleaned = text.text
            .replace(" ", "")
            .replace("-", "")
            .removePrefix("+258")
            .removePrefix("258")
            .take(9)

        val formatted = when {
            cleaned.startsWith("8") || cleaned.startsWith("2") -> {
                buildString {
                    cleaned.forEachIndexed { index, c ->
                        append(c)
                        if (index == 1 || index == 4) append(" ")
                    }
                }
            }
            cleaned.startsWith("800") -> {
                buildString {
                    cleaned.forEachIndexed { index, c ->
                        append(c)
                        if (index == 2 || index == 5) append(" ")
                    }
                }
            }
            else -> cleaned
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                var extra = 0
                if (cleaned.startsWith("8") || cleaned.startsWith("2")) {
                    if (offset > 1) extra++
                    if (offset > 4) extra++
                } else if (cleaned.startsWith("800")) {
                    if (offset > 2) extra++
                    if (offset > 5) extra++
                }
                return offset + extra
            }

            override fun transformedToOriginal(offset: Int): Int {
                var removed = 0
                if (cleaned.startsWith("8") || cleaned.startsWith("2")) {
                    if (offset > 2) removed++
                    if (offset > 5) removed++
                } else if (cleaned.startsWith("800")) {
                    if (offset > 3) removed++
                    if (offset > 6) removed++
                }
                return (offset - removed).coerceAtMost(cleaned.length)
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}