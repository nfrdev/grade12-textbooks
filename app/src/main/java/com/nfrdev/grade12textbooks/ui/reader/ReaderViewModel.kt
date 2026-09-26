package com.nfrdev.grade12textbooks.ui.reader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfrdev.grade12textbooks.data.local.dao.BookmarkDao
import com.nfrdev.grade12textbooks.data.local.dao.ProgressDao
import com.nfrdev.grade12textbooks.data.local.entity.BookmarkEntity
import com.nfrdev.grade12textbooks.data.local.entity.ProgressEntity
import com.nfrdev.grade12textbooks.domain.usecase.GetBookUseCase
import com.nfrdev.grade12textbooks.util.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import kotlin.math.min

@HiltViewModel
class ReaderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
    private val getBook: GetBookUseCase,
    private val progressDao: ProgressDao,
    private val bookmarkDao: BookmarkDao,
    private val userPreferences: UserPreferencesRepository
) : ViewModel() {
    private val bookId = savedStateHandle.get<String>("bookId").orEmpty()
    private val _state = MutableStateFlow<ReaderState>(ReaderState.Loading)
    val state: StateFlow<ReaderState> = _state

    private var renderer: PdfRenderer? = null
    private var descriptor: ParcelFileDescriptor? = null
    private val pageCache = BitmapPageCache(capacity = 5)
    private val renderMutex = Mutex()
    private var saveJob: Job? = null

    private var totalPages = 0
    private var currentPage = 0
    private val densityScale = (context.resources.displayMetrics.density * 1.5f).coerceAtLeast(1.0f)

    init {
        viewModelScope.launch { open() }
    }

    private suspend fun open() = withContext(Dispatchers.IO) {
        val book = getBook(bookId)
        val path = book?.localPath
        if (path == null || !File(path).exists()) {
            _state.value = ReaderState.Error
            return@withContext
        }
        try {
            descriptor = ParcelFileDescriptor.open(File(path), ParcelFileDescriptor.MODE_READ_ONLY)
            renderer = PdfRenderer(descriptor!!)
            totalPages = renderer!!.pageCount
            currentPage = progressDao.get(bookId)?.currentPage?.coerceIn(0, totalPages - 1) ?: 0
            renderCurrentPage()
        } catch (_: Exception) {
            _state.value = ReaderState.Error
        }
    }

    private suspend fun renderCurrentPage() = withContext(Dispatchers.IO) {
        val cached = synchronized(pageCache) { pageCache.get(currentPage) }
        if (cached != null) {
            _state.value = ReaderState.Ready(cached, currentPage, totalPages)
            saveProgressDebounced()
            preloadAdjacentPages()
            return@withContext
        }

        val bitmap = renderPageBitmap(currentPage) ?: run {
            _state.value = ReaderState.Error
            return@withContext
        }

        synchronized(pageCache) { pageCache.put(currentPage, bitmap) }
        _state.value = ReaderState.Ready(bitmap, currentPage, totalPages)
        saveProgressDebounced()
        preloadAdjacentPages()
    }

    private suspend fun renderPageBitmap(pageIndex: Int): Bitmap? = withContext(Dispatchers.IO) {
        renderMutex.withLock {
            val pdf = renderer ?: return@withLock null
            if (pageIndex !in 0 until totalPages) return@withLock null
            try {
                pdf.openPage(pageIndex).use { page ->
                    val renderWidth = min((page.width * densityScale).toInt(), 2048)
                    val renderHeight = min((page.height * densityScale).toInt(), 2048)
                    val bitmap = Bitmap.createBitmap(renderWidth, renderHeight, Bitmap.Config.ARGB_8888)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    bitmap
                }
            } catch (_: Exception) {
                null
            }
        }
    }

    private fun preloadAdjacentPages() {
        viewModelScope.launch(Dispatchers.IO) {
            val next = currentPage + 1
            if (next < totalPages && synchronized(pageCache) { pageCache.get(next) } == null) {
                renderPageBitmap(next)?.let { bmp ->
                    synchronized(pageCache) { pageCache.put(next, bmp) }
                }
            }
            val prev = currentPage - 1
            if (prev >= 0 && synchronized(pageCache) { pageCache.get(prev) } == null) {
                renderPageBitmap(prev)?.let { bmp ->
                    synchronized(pageCache) { pageCache.put(prev, bmp) }
                }
            }
        }
    }

    fun next() {
        if (currentPage < totalPages - 1) {
            viewModelScope.launch {
                currentPage++
                renderCurrentPage()
            }
        }
    }

    fun previous() {
        if (currentPage > 0) {
            viewModelScope.launch {
                currentPage--
                renderCurrentPage()
            }
        }
    }

    fun addBookmark(note: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            bookmarkDao.insert(
                BookmarkEntity(
                    bookId = bookId,
                    page = currentPage,
                    note = note,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun saveZoom(value: Float) {
        viewModelScope.launch { userPreferences.setReaderZoom(bookId, value) }
    }

    fun saveNightMode(value: Boolean) {
        viewModelScope.launch { userPreferences.setReaderNightMode(bookId, value) }
    }

    private fun saveProgressDebounced() {
        saveJob?.cancel()
        saveJob = viewModelScope.launch(Dispatchers.IO) {
            delay(500)
            progressDao.upsert(
                ProgressEntity(bookId, currentPage, totalPages, System.currentTimeMillis())
            )
        }
    }

    override fun onCleared() {
        renderer?.close()
        descriptor?.close()
        super.onCleared()
    }

    sealed interface ReaderState {
        data object Loading : ReaderState
        data object Error : ReaderState
        data class Ready(val bitmap: Bitmap, val page: Int, val total: Int) : ReaderState
    }
}
