package org.saudigitus.semis.core.designsystem.components.bottomsheet.model


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Save
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector


@Immutable
sealed class BottomSheetState(
    open val isLoading: Boolean,
    open val title: String,
    open val imageVector: ImageVector
) {
    @Immutable
    data class ErrorState(
        override val isLoading: Boolean,
        override val title: String = "",
        override val imageVector: ImageVector = Icons.Default.Error,
        val errorItems: List<Error> = emptyList()
    ): BottomSheetState(isLoading, title, imageVector)

    @Immutable
    data class HasItemsState(
        override val isLoading: Boolean = false,
        override val title: String = "",
        override val imageVector: ImageVector = Icons.Default.Save,
        val items: List<BottomSheetModel> = emptyList()
    ): BottomSheetState(isLoading, title, imageVector)

    @Immutable
    data class GenericsState<T>(
        override val isLoading: Boolean = false,
        override val title: String = "",
        override val imageVector: ImageVector = Icons.Default.Save,
        val items: List<T> = emptyList()
    ): BottomSheetState(isLoading, title, imageVector)
}