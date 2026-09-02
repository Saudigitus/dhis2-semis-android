package org.saudigitus.semis.core.utils

import kotlin.random.Random

object Utils {

    fun generateRandomId(length: Int = 11): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..length)
            .map { chars[Random.nextInt(chars.length)] }
            .joinToString("")
    }
}