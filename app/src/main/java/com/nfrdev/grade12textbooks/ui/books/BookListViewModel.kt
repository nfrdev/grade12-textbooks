package com.nfrdev.grade12textbooks.ui.books

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfrdev.grade12textbooks.domain.model.Book
import com.nfrdev.grade12textbooks.domain.model.Stream
import com.nfrdev.grade12textbooks.domain.repository.CatalogResult
import com.nfrdev.grade12textbooks.domain.usecase.GetBooksUseCase
import com.nfrdev.grade12textbooks.domain.usecase.RefreshCatalogUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@HiltViewModel
class BookListViewModel @Inject constructor(savedStateHandle: SavedStateHandle, private val getBooks: GetBooksUseCase, private val refreshCatalog: RefreshCatalogUseCase) : ViewModel() {
    private val stream = savedStateHandle.get<String>("stream").toStream()
    private val subjectId = savedStateHandle.get<String>("subjectId").orEmpty()
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
                is CatalogResult.Success -> _state.value = when (val result = getBooks(stream, subjectId)) {
                    is CatalogResult.Success -> if (result.value.isEmpty()) State.Empty else State.Success(result.value)
                    is CatalogResult.Failure -> State.Error(result.code)
                }
            }
        }
    }
    sealed interface State { data object Loading : State; data object Empty : State; data class Success(val books: List<Book>) : State; data class Error(val code: String) : State }
}

private fun String?.toStream() = when (this) {
    "social_science" -> Stream.SOCIAL_SCIENCE
    "common" -> Stream.COMMON
    else -> Stream.NATURAL_SCIENCE
}
