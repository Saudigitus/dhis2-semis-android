package org.saudigitus.semis.core.data.rules

/**
 * Restrictions that the option level program rules placed on a single field.
 *
 * DHIS2 narrows the options offered for a field in two opposite ways: by hiding single options or
 * whole option groups, or by revealing an option group. The two are not additive. Revealing a group
 * turns that group into the only valid choice for the field, which supersedes every hide effect, so
 * the three sets are kept apart instead of collapsed into one list of uids that could no longer tell
 * an option from a group nor a hide from a reveal.
 */
data class OptionRuleEffects(
    val optionsToHide: List<String> = emptyList(),
    val optionGroupsToHide: List<String> = emptyList(),
    val optionGroupsToShow: List<String> = emptyList(),
) {
    /**
     * True when no rule applied to the field, so the caller can serve the option set untouched
     * instead of resolving group membership it would never use.
     */
    val restrictsNothing: Boolean
        get() = optionsToHide.isEmpty() &&
            optionGroupsToHide.isEmpty() &&
            optionGroupsToShow.isEmpty()
}

/**
 * Decides whether [optionUid] survives the option level rules.
 *
 * [optionUidsToShow] is null when no rule revealed an option group. When it is present it decides
 * on its own and the hidden uids are not consulted, because revealing a group restricts the field
 * to that group rather than adding to what is already visible. Both sets hold option uids, so the
 * caller resolves group membership beforehand.
 */
internal fun isOptionVisible(
    optionUid: String,
    optionUidsToHide: Set<String>,
    optionUidsToShow: Set<String>?,
): Boolean = when (optionUidsToShow) {
    null -> optionUid !in optionUidsToHide
    else -> optionUid in optionUidsToShow
}
