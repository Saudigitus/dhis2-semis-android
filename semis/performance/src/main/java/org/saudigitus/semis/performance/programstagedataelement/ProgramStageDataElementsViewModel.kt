package org.saudigitus.semis.performance.programstagedataelement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.dhis2.commons.resources.ResourceManager
import org.saudigitus.semis.core.data.repository.ProgramStageRepository
import org.saudigitus.semis.core.designsystem.R
import org.saudigitus.semis.core.designsystem.components.model.ToolbarHeaders
import org.saudigitus.semis.core.designsystem.filters.FilterComponentState
import javax.inject.Inject


@HiltViewModel
class ProgramStageDataElementsViewModel @Inject constructor(
    private val programStageRepository: ProgramStageRepository,
    resourceManager: ResourceManager,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        ProgramStageDataElementsUiState(
            toolbarHeaders = ToolbarHeaders(title = resourceManager.getString(R.string.program_stage)),
        )
    )
    val uiState: StateFlow<ProgramStageDataElementsUiState> = _uiState.stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = _uiState.value
    )

    fun initialize(programStageId: String, filterState: FilterComponentState) {
        _uiState.update {
            it.copy(
                filterState = filterState
            )
        }
        loadProgramStagesDataElements(programStageId)
    }

    private fun loadProgramStagesDataElements(programStage: String) {
        viewModelScope.launch {
            val programStageDataElements =
                programStageRepository.getProgramStageDataElements(programStage)

            _uiState.update {
                it.copy(
                    programStageDataElements = programStageDataElements
                )
            }
        }
    }
}