package com.nfrdev.grade12textbooks.ui.downloads

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.nfrdev.grade12textbooks.data.download.DownloadScheduler
import com.nfrdev.grade12textbooks.data.download.DownloadWorker
import com.nfrdev.grade12textbooks.domain.model.Book
import com.nfrdev.grade12textbooks.domain.usecase.GetBookUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class DownloadItem(val book: Book, val state: WorkInfo.State, val progress: Float?, val error: String?)

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    application: Application,
    private val getBook: GetBookUseCase,
    private val scheduler: DownloadScheduler
) : AndroidViewModel(application) {
    private val workManager = WorkManager.getInstance(application)
    private val _items = MutableStateFlow<List<DownloadItem>>(emptyList())
    val items: StateFlow<List<DownloadItem>> = _items

    init {
        viewModelScope.launch {
            workManager.getWorkInfosByTagFlow(DownloadScheduler.TAG).collectLatest { work ->
                val visible = work.filter { it.state != WorkInfo.State.SUCCEEDED && it.state != WorkInfo.State.CANCELLED }
                val grouped = visible.mapNotNull { info ->
                    val bookId = info.tags.firstOrNull { it.startsWith("book-download:") }?.removePrefix("book-download:") ?: return@mapNotNull null
                    bookId to info
                }.groupBy({ it.first }, { it.second })
                _items.value = grouped.mapNotNull { (bookId, attempts) ->
                    val info = attempts.firstOrNull { it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.BLOCKED } ?: attempts.last()
                    val book = getBook(bookId) ?: return@mapNotNull null
                    DownloadItem(book, info.state, info.progress.getFloat(DownloadWorker.KEY_PROGRESS, -1f).takeIf { it >= 0f }, info.outputData.getString(DownloadWorker.KEY_ERROR))
                }
            }
        }
    }

    fun cancel(bookId: String) = scheduler.cancel(bookId)

}
