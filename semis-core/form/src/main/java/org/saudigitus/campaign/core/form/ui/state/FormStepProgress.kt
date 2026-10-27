package org.saudigitus.campaign.core.form.ui.state

import androidx.compose.runtime.Immutable

/**
 * Where a form sits within a longer flow.
 *
 * The form itself has no notion of what comes before or after it, so whoever drives the flow passes
 * this in. It is what lets the header say how far along the user is and, more importantly, lets the
 * action read as moving on rather than finishing on every step but the last.
 */
@Immutable
data class FormStepProgress(
    val stepNumber: Int,
    val stepCount: Int,
) {
    /** True on the step whose action commits, rather than advancing to another form. */
    val isLast: Boolean get() = stepNumber >= stepCount

    /** How much of the flow is behind the user, for the progress indicator. */
    val fraction: Float
        get() = if (stepCount <= 0) 0f else stepNumber.toFloat() / stepCount.toFloat()
}
