package org.saudigitus.campaign.core.utils

import java.util.Collections
import kotlin.random.Random

object IdGenerator {
    fun generate(): String {
        val chars = ('A'..'Z') + ('0'..'9')
        val generated = mutableSetOf<String>()

        var id: String
        do {
            id = (1..3).joinToString("-") {
                (1..4).map { chars.random() }.joinToString("")
            }
        } while (!generated.add(id))
        return id
    }

    fun generateDhis2PatternId(length: Int = 11): String {
        val letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        val generated = Collections.synchronizedSet(mutableSetOf<String>())

        var id: String
        do {
            val first = letters[Random.nextInt(letters.length)]
            val rest = (2..length).map { chars[Random.nextInt(chars.length)] }
            id = (listOf(first) + rest).joinToString("")
        } while (!generated.add(id))
        return id
    }
}