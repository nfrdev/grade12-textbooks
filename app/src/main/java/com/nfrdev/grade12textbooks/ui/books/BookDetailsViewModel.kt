package com.nfrdev.grade12textbooks.ui.books

import androidx.lifecycle.SavedStateHandle
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nfrdev.grade12textbooks.data.download.BookDownloader
import com.nfrdev.grade12textbooks.data.download.DownloadState
import com.nfrdev.grade12textbooks.data.download.DownloadScheduler
import com.nfrdev.grade12textbooks.data.download.DownloadWorker
import com.nfrdev.grade12textbooks.domain.model.Book
import com.nfrdev.grade12textbooks.domain.usecase.GetBookUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.work.WorkInfo
import androidx.work.WorkManager

@HiltViewModel
class BookDetailsViewModel @Inject constructor(
    application: Application,
    savedStateHandle: SavedStateHandle,
    private val getBook: GetBookUseCase,
    private val downloader: BookDownloader,
    private val scheduler: DownloadScheduler
) : AndroidViewModel(application) {
    private val bookId = savedStateHandle.get<String>("bookId").orEmpty()
    private val _book = MutableStateFlow<Book?>(null)
    val book: StateFlow<Book?> = _book
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading
    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState
    init {
        viewModelScope.launch {
            _book.value = getBook(bookId)
            _isLoading.value = false
            launch { downloader.state(bookId).collect { _downloadState.value = it } }
            WorkManager.getInstance(application).getWorkInfosForUniqueWorkFlow(DownloadScheduler.workName(bookId)).collect { work ->
                val latest = work.lastOrNull() ?: return@collect
                _downloadState.value = when (latest.state) {
                    WorkInfo.State.ENQUEUED, WorkInfo.State.BLOCKED -> DownloadState.Queued
                    WorkInfo.State.RUNNING -> _downloadState.value
                    WorkInfo.State.SUCCEEDED -> DownloadState.Completed
                    WorkInfo.State.FAILED -> DownloadState.Failed(latest.outputData.getString(DownloadWorker.KEY_ERROR) ?: "download_failed")
                    WorkInfo.State.CANCELLED -> if (_downloadState.value == DownloadState.Paused) DownloadState.Paused else DownloadState.Idle
                }
            }
        }
    }
    fun download() { _book.value?.let { book -> viewModelScope.launch { scheduler.start(book) } } }
    fun pause() = scheduler.pause(bookId)
    fun cancel() = scheduler.cancel(bookId)
}
