package com.nfrdev.grade12textbooks.data.download

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.BackoffPolicy
import androidx.work.workDataOf
import com.nfrdev.grade12textbooks.domain.model.Book
import com.nfrdev.grade12textbooks.util.PreferenceKeys
import com.nfrdev.grade12textbooks.util.userPreferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val downloader: BookDownloader
) {
    suspend fun start(book: Book) {
        val wifiOnly = context.userPreferencesDataStore.data.first()[PreferenceKeys.wifiOnly] ?: true
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED)
            .setRequiresStorageNotLow(true)
            .build()
        val request = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(workDataOf(DownloadWorker.KEY_BOOK_ID to book.id))
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .addTag(TAG)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(workName(book.id), ExistingWorkPolicy.KEEP, request)
    }

    fun pause(bookId: String) {
        WorkManager.getInstance(context).cancelUniqueWork(workName(bookId))
        downloader.pause(bookId)
    }

    suspend fun resume(book: Book) {
        downloader.pause(book.id)
        start(book)
    }

    fun cancel(bookId: String) {
        WorkManager.getInstance(context).cancelUniqueWork(workName(bookId))
        downloader.cancel(bookId)
    }

    companion object {
        const val TAG = "book-download"
        fun workName(bookId: String) = "download:$bookId"
    }
}
