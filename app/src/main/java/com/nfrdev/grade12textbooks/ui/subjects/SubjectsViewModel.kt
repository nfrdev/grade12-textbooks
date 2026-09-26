package com.nfrdev.grade12textbooks.ui.subjects

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfrdev.grade12textbooks.domain.model.Stream
import com.nfrdev.grade12textbooks.domain.model.Subject
import com.nfrdev.grade12textbooks.domain.repository.CatalogResult
import com.nfrdev.grade12textbooks.domain.usecase.GetSubjectsUseCase
import com.nfrdev.grade12textbooks.domain.usecase.RefreshCatalogUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@HiltViewModel
class SubjectsViewModel @Inject constructor(savedStateHandle: SavedStateHandle, private val getSubjects: GetSubjectsUseCase, private val refreshCatalog: RefreshCatalogUseCase) : ViewModel() {
    private val stream = savedStateHandle.get<String>("stream")?.toStream() ?: Stream.NATURAL_SCIENCE
    private val _state = MutableStateFlow<State>(State.Loading)
    val state: StateFlow<State> = _state
    private var loadJob: Job? = null

    init { retry() }

    fun retry() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.value = State.Loading
            when (val refresh = refreshCatalog()) {
                is CatalogResult.Failure -> _state.value = State.Error(refresh.code)
                is CatalogResult.Success -> _state.value = when (val result = getSubjects(stream)) {
                    is CatalogResult.Success -> if (result.value.isEmpty()) State.Empty else State.Success(result.value)
                    is CatalogResult.Failure -> State.Error(result.code)
                }
            }
        }
    }
    sealed interface State { data object Loading : State; data object Empty : State; data class Success(val subjects: List<Subject>) : State; data class Error(val code: String) : State }
}

private fun String.toStream() = when (this) {
    "social_science" -> Stream.SOCIAL_SCIENCE
    "common" -> Stream.COMMON
    else -> Stream.NATURAL_SCIENCE
}
