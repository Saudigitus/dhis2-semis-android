package org.saudigitus.semis.core.designsystem.components.bottomsheet.model


import androidx.compose.runtime.Immutable


@Immutable
sealed class BottomSheetState(
    open val isLoading: Boolean,
    open val title: String,
) {
    @Immutable
    data class ErrorState(
        override val isLoading: Boolean,
        override val title: String = "",
        val prescriptions: List<Error> = emptyList()
    ): BottomSheetState(isLoading, title)

    @Immutable
    data class SaveState(
        override val isLoading: Boolean = false,
        override val title: String = "",
        val indicators: List<SummaryIndicator> = emptyList()
    ): BottomSheetState(isLoading, title)
}