package org.saudigitus.semis.transfer.components.list

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import org.saudigitus.semis.core.data.model.transfer.TransferStatus
import org.saudigitus.semis.core.designsystem.theme.SemisAccent
import org.saudigitus.semis.core.designsystem.theme.SemisPalette
import org.saudigitus.semis.transfer.R
import org.saudigitus.semis.transfer.model.RelativeTimeUnit

/** Colour a transfer status is shown in, so the state reads before the word does. */
internal val TransferStatus.accentColor: Color
    get() = when (this) {
        TransferStatus.PENDING -> SemisAccent.Amber
        TransferStatus.APPROVED -> SemisAccent.Green
        TransferStatus.REJECTED -> SemisAccent.Red
        TransferStatus.UNKNOWN -> SemisPalette.TextSecondary
    }

@get:StringRes
internal val TransferStatus.label: Int
    get() = when (this) {
        TransferStatus.PENDING -> R.string.transfer_status_pending
        TransferStatus.APPROVED -> R.string.transfer_status_approved
        TransferStatus.REJECTED -> R.string.transfer_status_rejected
        TransferStatus.UNKNOWN -> R.string.transfer_status_unknown
    }

/**
 * Plural resource for a relative time. [RelativeTimeUnit.JUST_NOW] carries no amount, so
 * it maps to a plain string the caller reads without a quantity.
 */
@get:StringRes
internal val RelativeTimeUnit.pluralLabel: Int
    get() = when (this) {
        RelativeTimeUnit.JUST_NOW -> R.string.relative_time_just_now
        RelativeTimeUnit.MINUTES -> R.plurals.relative_time_minutes
        RelativeTimeUnit.HOURS -> R.plurals.relative_time_hours
        RelativeTimeUnit.DAYS -> R.plurals.relative_time_days
        RelativeTimeUnit.WEEKS -> R.plurals.relative_time_weeks
        RelativeTimeUnit.MONTHS -> R.plurals.relative_time_months
        RelativeTimeUnit.YEARS -> R.plurals.relative_time_years
    }
