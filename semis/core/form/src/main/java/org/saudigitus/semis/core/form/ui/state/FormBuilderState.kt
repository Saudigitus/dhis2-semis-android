package org.saudigitus.semis.core.form.ui.state

import androidx.compose.runtime.Immutable

@Immutable
data class FormBuilderState(
    val orgUnit: String = "",
    val program: String = "",
    val programStage: String = ""
)
