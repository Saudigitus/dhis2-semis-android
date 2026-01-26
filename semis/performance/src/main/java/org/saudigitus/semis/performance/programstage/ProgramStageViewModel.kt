package org.saudigitus.semis.performance.programstage

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
import org.saudigitus.semis.core.data.repository.AppConfigRepository
import org.saudigitus.semis.core.data.repository.ProgramStageRepository
import org.saudigitus.semis.core.designsystem.R
import org.saudigitus.semis.core.designsystem.components.model.ToolbarHeaders
import org.saudigitus.semis.core.designsystem.filters.FilterComponentState
import org.saudigitus.semis.performance.utils.toProgramStageModel
import javax.inject.Inject

@HiltViewModel
class ProgramStageViewModel @Inject constructor(
    private val programStageRepository: ProgramStageRepository,
    private val appConfigRepository: AppConfigRepository,
    resourceManager: ResourceManager,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        ProgramStageUiState(
            toolbarHeaders = ToolbarHeaders(title = resourceManager.getString(R.string.program_stages)),
        )
    )
    val uiState: StateFlow<ProgramStageUiState> = _uiState.stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = _uiState.value
    )

    fun initialize(
        program: String,
        filterState: FilterComponentState,
    ) {
        _uiState.update {
            it.copy(
                program = program,
                filterState = filterState
            )
        }
        loadProgramStages(program)
    }

    private fun loadProgramStages(program: String) {
        viewModelScope.launch {
            val ps =
                appConfigRepository.getAppConfig(program)?.performance?.programStages
                    ?.filterNotNull()
                    ?: emptyList()
            val programStages =
                programStageRepository.getProgramStagesByIds(ps)
                    .toProgramStageModel()
            _uiState.update {
                it.copy(
                    programStages = programStages
                )
            }
        }
    }


}