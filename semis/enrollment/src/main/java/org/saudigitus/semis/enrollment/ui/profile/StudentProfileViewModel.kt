package org.saudigitus.semis.enrollment.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.saudigitus.semis.core.data.repository.TeiProfileRepository
import org.saudigitus.semis.core.designsystem.components.FilterDetailsState
import javax.inject.Inject

@HiltViewModel
class StudentProfileViewModel @Inject constructor(
    private val repository: TeiProfileRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentProfileUiState())
    val uiState: StateFlow<StudentProfileUiState> = _uiState.asStateFlow()

    private var initialized = false

    /**
     * Loads the dashboard for [teiUid] under the selection the home screen is on, so the
     * history shown belongs to the same academic year, school, grade and section.
     */
    fun initialize(
        teiUid: String,
        program: String,
        filterDetailsState: FilterDetailsState,
    ) {
        if (initialized) return
        initialized = true

        _uiState.update {
            it.copy(
                isLoading = true,
                filterDetailsState = filterDetailsState.copy(
                    enable = false,
                    enableCounter = false,
                ),
            )
        }

        viewModelScope.launch {
            runCatching {
                repository.getProfile(
                    teiUid = teiUid,
                    program = program,
                    academicYear = filterDetailsState.academicYear,
                )
            }.onSuccess { profile ->
                _uiState.update { it.copy(isLoading = false, profile = profile) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = error.message)
                }
            }
        }
    }

    fun handleEvent(event: StudentProfileEvent) {
        when (event) {
            is StudentProfileEvent.SelectTab -> {
                _uiState.update { it.copy(selectedTab = event.tab) }
            }

            StudentProfileEvent.OnBack -> Unit
        }
    }
}
