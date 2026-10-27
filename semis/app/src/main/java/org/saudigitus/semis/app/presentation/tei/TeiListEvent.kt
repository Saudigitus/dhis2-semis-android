package org.saudigitus.semis.app.presentation.tei

sealed interface TeiListEvent {
    data object OnBack: TeiListEvent
    data object OnSyncClick: TeiListEvent
    data class OnTeiClick(val tei: String, val enrollment: String): TeiListEvent
    data class DisplayImageDetail(val imagePath: String): TeiListEvent
}