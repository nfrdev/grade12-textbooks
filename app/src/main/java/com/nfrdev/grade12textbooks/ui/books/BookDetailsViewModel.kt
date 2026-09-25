package com.nfrdev.grade12textbooks.ui.books

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfrdev.grade12textbooks.data.download.BookDownloader
import com.nfrdev.grade12textbooks.data.download.DownloadState
import com.nfrdev.grade12textbooks.data.download.DownloadScheduler
import com.nfrdev.grade12textbooks.domain.model.Book
import com.nfrdev.grade12textbooks.domain.usecase.GetBookUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class BookDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getBook: GetBookUseCase,
    private val downloader: BookDownloader,
    private val scheduler: DownloadScheduler
) : ViewModel() {
    private val bookId = savedStateHandle.get<String>("bookId").orEmpty()
    private val _book = MutableStateFlow<Book?>(null)
    val book: StateFlow<Book?> = _book
    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState
    init { viewModelScope.launch { _book.value = getBook(bookId); downloader.state(bookId).collect { _downloadState.value = it } } }
    fun download() { _book.value?.let { book -> viewModelScope.launch { scheduler.start(book) } } }
    fun pause() = scheduler.pause(bookId)
    fun cancel() = scheduler.cancel(bookId)
}
