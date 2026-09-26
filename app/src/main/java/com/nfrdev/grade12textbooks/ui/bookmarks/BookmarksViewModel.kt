package com.nfrdev.grade12textbooks.ui.bookmarks

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfrdev.grade12textbooks.data.local.dao.BookmarkDao
import com.nfrdev.grade12textbooks.data.local.entity.BookmarkEntity
import com.nfrdev.grade12textbooks.domain.usecase.GetBookUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getBook: GetBookUseCase,
    private val bookmarkDao: BookmarkDao
) : ViewModel() {
    private val bookId = savedStateHandle.get<String>("bookId").orEmpty()
    private val _state = MutableStateFlow<State>(State.Loading)
    val state: StateFlow<State> = _state

    init {
        load()
    }

    fun retry() = load()

    fun delete(bookmark: BookmarkEntity) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { bookmarkDao.delete(bookmark) }
            load()
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.value = State.Loading
            runCatching {
                val book = getBook(bookId) ?: return@runCatching null
                book.title to withContext(Dispatchers.IO) { bookmarkDao.forBook(bookId) }
            }.onSuccess { result ->
                _state.value = when {
                    result == null -> State.Error
                    result.second.isEmpty() -> State.Empty(result.first)
                    else -> State.Success(result.first, result.second)
                }
            }.onFailure {
                _state.value = State.Error
            }
        }
    }

    sealed interface State {
        data object Loading : State
        data object Error : State
        data class Empty(val bookTitle: String) : State
        data class Success(val bookTitle: String, val bookmarks: List<BookmarkEntity>) : State
    }
}
