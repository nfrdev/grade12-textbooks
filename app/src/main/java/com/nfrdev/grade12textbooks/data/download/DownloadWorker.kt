package com.nfrdev.grade12textbooks.data.download

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nfrdev.grade12textbooks.domain.repository.CatalogRepository
import com.nfrdev.grade12textbooks.util.Logger
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect

@HiltWorker
class DownloadWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repository: CatalogRepository,
    private val downloader: BookDownloader,
    private val slots: DownloadSlotDao,
    private val logger: Logger
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val bookId = inputData.getString(KEY_BOOK_ID) ?: return Result.failure()
        if (slots.tryClaim(bookId, System.currentTimeMillis()) == -1L && slots.isClaimed(bookId) == 0) return Result.retry()
        return try {
            val book = repository.getBooks().firstOrNull { it.id == bookId }
            if (book == null) {
                logger.e("Download failed: book $bookId is missing from the catalog")
                return Result.failure(androidx.work.workDataOf(KEY_ERROR to "book_not_found"))
            }
            val job: Job = downloader.start(book)
            val progressJob = CoroutineScope(currentCoroutineContext()).launch {
                downloader.state(bookId).collect { state ->
                    val percent = (state as? DownloadState.InProgress)?.progress
                    if (percent != null) setProgress(androidx.work.workDataOf(KEY_PROGRESS to percent))
                }
            }
            job.join()
            progressJob.cancel()
            val finalState = downloader.stateValue(bookId)
            when (finalState) {
                DownloadState.Completed -> Result.success()
                DownloadState.Paused -> Result.retry()
                is DownloadState.Failed -> {
                    logger.e("Download failed for $bookId: ${finalState.reason}")
                    if (isRetryable(finalState.reason)) Result.retry() else Result.failure(androidx.work.workDataOf(KEY_ERROR to finalState.reason))
                }
                else -> Result.failure(androidx.work.workDataOf(KEY_ERROR to "unexpected_download_state"))
            }
        } catch (cancelled: CancellationException) {
            if (downloader.stateValue(bookId) != DownloadState.Idle) downloader.pause(bookId)
            throw cancelled
        } finally { slots.release(bookId) }
    }
    private fun isRetryable(reason: String): Boolean = reason == "network_error" || reason == "http_408" || reason == "http_429" ||
        reason.removePrefix("http_").toIntOrNull()?.let { it >= 500 } == true

    companion object { const val KEY_BOOK_ID = "book_id"; const val KEY_PROGRESS = "progress"; const val KEY_ERROR = "error" }
}
