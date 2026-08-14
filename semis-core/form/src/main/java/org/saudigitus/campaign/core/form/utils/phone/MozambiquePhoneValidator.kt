package org.saudigitus.campaign.core.form.utils.phone

object MozambiquePhoneValidator {

    const val MAX_LEN = 9


    private val mobileRegex = Regex("^8[2-7][0-9]{7}$")

    private val landlineRegex = Regex("^2[1-9][0-9]{7}$")

    private val tollFreeRegex = Regex("^800[0-9]{6}$")

    fun clean(input: String): String {
        return input
            .replace(" ", "")
            .replace("-", "")
            .removePrefix("+258")
            .removePrefix("258")
    }

    fun isValid(input: String): Boolean {
        val cleaned = clean(input)
        return mobileRegex.matches(cleaned)
            || landlineRegex.matches(cleaned)
            || tollFreeRegex.matches(cleaned)
    }

    fun getType(input: String): PhoneNumberType {
        val cleaned = clean(input)
        return when {
            mobileRegex.matches(cleaned) -> PhoneNumberType.MOBILE
            landlineRegex.matches(cleaned) -> PhoneNumberType.LANDLINE
            tollFreeRegex.matches(cleaned) -> PhoneNumberType.TOLL_FREE
            else -> PhoneNumberType.UNKNOWN
        }
    }
}