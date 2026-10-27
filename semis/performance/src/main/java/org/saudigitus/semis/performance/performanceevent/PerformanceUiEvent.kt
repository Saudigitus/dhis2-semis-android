package org.saudigitus.semis.performance.performanceevent

sealed class PerformanceUiEvent {
    data object NavBack : PerformanceUiEvent()
    data object Sync : PerformanceUiEvent()
    data object EditEvent : PerformanceUiEvent()
    data object CancelEventData : PerformanceUiEvent()
    data object ConfirmEventData : PerformanceUiEvent()
    data object SaveEvent : PerformanceUiEvent()
}