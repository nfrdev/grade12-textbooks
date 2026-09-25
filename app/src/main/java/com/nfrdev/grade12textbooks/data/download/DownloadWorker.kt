package com.nfrdev.grade12textbooks.data.download

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nfrdev.grade12textbooks.domain.repository.CatalogRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Job

@HiltWorker
class DownloadWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repository: CatalogRepository,
    private val downloader: BookDownloader,
    private val slots: DownloadSlotDao
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val bookId = inputData.getString(KEY_BOOK_ID) ?: return Result.failure()
        if (slots.tryClaim(bookId, System.currentTimeMillis()) == -1L && slots.isClaimed(bookId) == 0) return Result.retry()
        return try {
            val book = repository.getBooks().firstOrNull { it.id == bookId } ?: return Result.failure()
            val job: Job = downloader.start(book)
            job.join()
            val finalState = downloader.stateValue(bookId)
            when (finalState) {
                DownloadState.Completed -> Result.success()
                DownloadState.Paused -> Result.retry()
                is DownloadState.Failed -> if (finalState.reason == "checksum_mismatch") Result.failure() else Result.retry()
                else -> Result.retry()
            }
        } finally { slots.release(bookId) }
    }
    companion object { const val KEY_BOOK_ID = "book_id" }
}
