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
     * Loads the page of [teiUid] for the academic year the home filters are on, which is the year
     * of the enrollment whose records the page shows.
     */
    fun initialize(
        teiUid: String,
        program: String,
        filterDetailsState: FilterDetailsState,
    ) {
        if (initialized) return
        initialized = true

        _uiState.update { it.copy(isLoading = true, teiUid = teiUid) }

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

            // Leaving the page and sending what the device holds are both answered by whoever
            // hosts the page, because neither is a question about the record being read.
            StudentProfileEvent.OnBack,
            StudentProfileEvent.OnSyncClick,
            -> Unit
        }
    }
}
