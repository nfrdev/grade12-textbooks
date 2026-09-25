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
import com.nfrdev.grade12textbooks.domain.model.Book
import com.nfrdev.grade12textbooks.domain.usecase.GetBookUseCase
import com.nfrdev.grade12textbooks.util.PreferenceKeys
import com.nfrdev.grade12textbooks.util.userPreferencesDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.datastore.preferences.core.toMutablePreferences
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ReaderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
    private val getBook: GetBookUseCase,
    private val progressDao: ProgressDao,
    private val bookmarkDao: BookmarkDao
) : ViewModel() {
    private val bookId = savedStateHandle.get<String>("bookId").orEmpty()
    private val _state = MutableStateFlow<ReaderState>(ReaderState.Loading)
    val state: StateFlow<ReaderState> = _state
    private var renderer: PdfRenderer? = null
    private var descriptor: ParcelFileDescriptor? = null
    private var saveJob: Job? = null
    private var totalPages = 0
    private var currentPage = 0
    init { viewModelScope.launch { open() } }

    private suspend fun open() = withContext(Dispatchers.IO) {
        val book = getBook(bookId)
        val path = book?.localPath
        if (path == null || !File(path).exists()) { _state.value = ReaderState.Error; return@withContext }
        try {
            descriptor = ParcelFileDescriptor.open(File(path), ParcelFileDescriptor.MODE_READ_ONLY)
            renderer = PdfRenderer(descriptor!!)
            totalPages = renderer!!.pageCount
            currentPage = progressDao.get(bookId)?.currentPage?.coerceIn(0, totalPages - 1) ?: 0
            render()
        } catch (_: Exception) { _state.value = ReaderState.Error }
    }

    private suspend fun render() = withContext(Dispatchers.IO) {
        val pdf = renderer ?: return@withContext
        val page = pdf.openPage(currentPage)
        val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        page.close()
        _state.value = ReaderState.Ready(bitmap, currentPage, totalPages)
        saveProgressDebounced()
    }

    fun next() { if (currentPage < totalPages - 1) viewModelScope.launch { currentPage++; render() } }
    fun previous() { if (currentPage > 0) viewModelScope.launch { currentPage--; render() } }
    fun addBookmark(note: String?) { viewModelScope.launch(Dispatchers.IO) { bookmarkDao.insert(BookmarkEntity(bookId = bookId, page = currentPage, note = note, createdAt = System.currentTimeMillis())) } }
    fun saveZoom(value: Float) { viewModelScope.launch { context.userPreferencesDataStore.updateData { it.toMutablePreferences().apply { this[PreferenceKeys.readerZoom(bookId)] = value } } } }
    fun saveNightMode(value: Boolean) { viewModelScope.launch { context.userPreferencesDataStore.updateData { it.toMutablePreferences().apply { this[PreferenceKeys.readerNightMode(bookId)] = value } } } }
    private fun saveProgressDebounced() { saveJob?.cancel(); saveJob = viewModelScope.launch(Dispatchers.IO) { delay(500); progressDao.upsert(ProgressEntity(bookId, currentPage, totalPages, System.currentTimeMillis())) } }
    override fun onCleared() { renderer?.close(); descriptor?.close(); super.onCleared() }

    sealed interface ReaderState { data object Loading : ReaderState; data object Error : ReaderState; data class Ready(val bitmap: Bitmap, val page: Int, val total: Int) : ReaderState }
}
