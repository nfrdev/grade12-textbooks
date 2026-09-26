package com.nfrdev.grade12textbooks.data.download

import android.content.Context
import com.nfrdev.grade12textbooks.BuildConfig
import com.nfrdev.grade12textbooks.data.local.dao.BookLocalStateDao
import com.nfrdev.grade12textbooks.data.local.entity.BookLocalStateEntity
import com.nfrdev.grade12textbooks.domain.model.Book
import com.nfrdev.grade12textbooks.util.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.net.URI
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

@Singleton
class BookDownloader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val client: OkHttpClient,
    private val localStateDao: BookLocalStateDao,
    private val logger: Logger
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val jobs = mutableMapOf<String, Job>()
    private val paused = mutableSetOf<String>()
    private val stateMap = mutableMapOf<String, MutableStateFlow<DownloadState>>()

    fun state(bookId: String): Flow<DownloadState> = flowFor(bookId).asStateFlow()
    fun stateValue(bookId: String): DownloadState = flowFor(bookId).value

    fun start(book: Book): Job = synchronized(jobs) {
        jobs[book.id]?.cancel()
        paused.remove(book.id)
        flowFor(book.id).value = DownloadState.Queued
        scope.launch { runDownload(book) }.also { jobs[book.id] = it }
    }

    fun pause(bookId: String) = synchronized(jobs) {
        paused += bookId
        jobs.remove(bookId)?.cancel()
        flowFor(bookId).value = DownloadState.Paused
    }

    fun cancel(bookId: String) = synchronized(jobs) {
        paused.remove(bookId)
        jobs.remove(bookId)?.cancel()
        filesFor(bookId).part.delete()
        flowFor(bookId).value = DownloadState.Idle
    }

    private suspend fun runDownload(book: Book) = withContext(Dispatchers.IO) {
        val files = filesFor(book.id)
        try {
            if (!validUrl(book.pdfUrl)) throw DownloadException("unsupported_url")
            if (!files.dir.exists() && !files.dir.mkdirs()) throw DownloadException("storage_unavailable")
            if (!hasStorage(book)) throw DownloadException("insufficient_storage")
            if (files.final.exists() && verify(files.final, book.checksumSha256)) {
                persistCompleted(book, files.final)
                flowFor(book.id).value = DownloadState.Completed
                return@withContext
            } else if (files.final.exists()) files.final.delete()

            val existing = files.part.length()
            val requestBuilder = Request.Builder().url(book.pdfUrl)
            if (existing > 0) requestBuilder.header("Range", "bytes=$existing-")
            var completeOn416 = false
            client.newCall(requestBuilder.build()).execute().use { response ->
                when (response.code) {
                    416 -> {
                        if (book.fileSize != null && existing == book.fileSize) completeOn416 = true
                        else throw DownloadException("range_not_satisfiable")
                    }
                    200 -> {
                        if (existing > 0) files.part.outputStream().use { }
                        transfer(book, files, response, append = false)
                    }
                    206 -> {
                        val start = response.header("Content-Range")?.substringAfter("bytes ")?.substringBefore("-")?.toLongOrNull()
                        if (existing > 0 && start != existing) throw DownloadException("inconsistent_range")
                        transfer(book, files, response, append = existing > 0)
                    }
                    else -> throw DownloadException("http_${response.code}")
                }
            }
            if (completeOn416) verifyOrFail(book, files) else if (flowFor(book.id).value != DownloadState.Completed) verifyOrFail(book, files)
        } catch (e: CancellationException) {
            val isPaused = synchronized(jobs) { paused.contains(book.id) }
            if (isPaused) flowFor(book.id).value = DownloadState.Paused
            else throw e
        } catch (e: DownloadException) {
            flowFor(book.id).value = DownloadState.Failed(e.reason)
        } catch (e: Exception) {
            logger.e("Download failed for ${book.id}", e)
            flowFor(book.id).value = DownloadState.Failed("network_error")
        } finally {
            synchronized(jobs) { jobs.remove(book.id) }
        }
    }

    private fun transfer(book: Book, files: DownloadFiles, response: okhttp3.Response, append: Boolean) {
        val body = response.body ?: throw DownloadException("empty_response")
        val startLength = if (append) files.part.length() else 0L
        if (!append) files.part.outputStream().use { }
        val total = book.fileSize ?: body.contentLength().takeIf { it > 0 }?.let { it + startLength }
        body.byteStream().use { input ->
            FileOutputStream(files.part, append).buffered().use { output ->
                val buffer = ByteArray(8192)
                var written = startLength
                while (true) {
                    val read = input.read(buffer)
                    if (read <= 0) break
                    written += read
                    if (written > MAX_ACCEPTED_BYTES || (book.fileSize != null && written > book.fileSize * 1.05 + 1)) throw DownloadException("download_too_large")
                    output.write(buffer, 0, read)
                    flowFor(book.id).value = DownloadState.InProgress(total?.let { min(1f, written.toFloat() / it) })
                }
            }
        }
    }

    private suspend fun verifyOrFail(book: Book, files: DownloadFiles) {
        flowFor(book.id).value = DownloadState.Verifying
        if (!verify(files.part, book.checksumSha256)) {
            files.part.delete()
            throw DownloadException("checksum_mismatch")
        }
        if (!files.part.renameTo(files.final)) throw DownloadException("file_promotion_failed")
        persistCompleted(book, files.final)
        flowFor(book.id).value = DownloadState.Completed
    }

    private suspend fun persistCompleted(book: Book, file: File) {
        localStateDao.upsert(BookLocalStateEntity(book.id, file.absolutePath, true, System.currentTimeMillis(), book.lastOpenedAt, book.isFavorite, book.isOrphaned))
    }

    private fun verify(file: File, expected: String): Boolean {
        if (!file.exists()) return false
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            while (true) { val n = input.read(buffer); if (n <= 0) break; digest.update(buffer, 0, n) }
        }
        return digest.digest().joinToString("") { "%02x".format(it) } == expected.lowercase()
    }

    private fun hasStorage(book: Book): Boolean {
        val usable = filesFor(book.id).dir.usableSpace
        val required = (book.fileSize ?: UNKNOWN_SIZE) + SAFETY_MARGIN
        return usable >= required
    }

    private fun validUrl(url: String): Boolean = try {
        val uri = URI(url); val scheme = uri.scheme?.lowercase()
        scheme == "https" || (BuildConfig.DEBUG && scheme == "http" && (uri.host == "localhost" || uri.host == "10.0.2.2"))
    } catch (_: Exception) { false }

    private fun flowFor(id: String) = synchronized(stateMap) { stateMap.getOrPut(id) { MutableStateFlow(DownloadState.Idle) } }
    private fun filesFor(id: String): DownloadFiles {
        require(id.matches(Regex("[A-Za-z0-9._-]+"))) { "invalid_book_id" }
        val dir = File(context.getExternalFilesDir(null) ?: context.filesDir, "books")
        return DownloadFiles(dir, File(dir, "$id.pdf.part"), File(dir, "$id.pdf"))
    }

    private data class DownloadFiles(val dir: File, val part: File, val final: File)
    private class DownloadException(val reason: String) : Exception(reason)
    companion object { const val MAX_ACCEPTED_BYTES = 250L * 1024 * 1024; const val SAFETY_MARGIN = 50L * 1024 * 1024; const val UNKNOWN_SIZE = MAX_ACCEPTED_BYTES }
}
