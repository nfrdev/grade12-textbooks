package com.nfrdev.grade12textbooks.data.remote

import com.nfrdev.grade12textbooks.BuildConfig
import com.nfrdev.grade12textbooks.data.remote.dto.BookDto
import com.nfrdev.grade12textbooks.data.remote.dto.CatalogDto
import com.nfrdev.grade12textbooks.data.local.entity.BookEntity
import com.nfrdev.grade12textbooks.util.Logger
import kotlinx.serialization.json.Json
import java.net.URI
import java.text.SimpleDateFormat
import java.util.Locale

sealed interface CatalogParseResult {
    data class Success(val version: Long, val books: List<BookEntity>) : CatalogParseResult
    data class Failure(val reason: String) : CatalogParseResult
}

class CatalogParser(private val json: Json, private val logger: Logger) {
    fun parse(raw: String): CatalogParseResult = try {
        val dto = json.decodeFromString<CatalogDto>(raw)
        if (dto.schemaVersion != 1) return CatalogParseResult.Failure("unsupported_schema")
        val ids = HashSet<String>()
        val valid = dto.books.mapNotNull { book -> validate(book, ids) }
        if (valid.isEmpty()) CatalogParseResult.Failure("no_valid_books")
        else CatalogParseResult.Success(dto.version, valid)
    } catch (e: Exception) {
        logger.e("Catalog parsing failed", e)
        CatalogParseResult.Failure("invalid_catalog")
    }

    private fun validate(book: BookDto, ids: MutableSet<String>): BookEntity? {
        fun reject(reason: String): BookEntity? {
            logger.e("Skipping catalog book ${book.id.ifBlank { "<empty>" }}: $reason")
            return null
        }
        if (book.id.isBlank() || !ids.add(book.id)) return reject("duplicate_or_empty_id")
        if (book.title.isBlank() || book.stream !in setOf("natural_science", "social_science") || book.subjectId.isBlank()) return reject("required_field")
        if (book.language.isBlank() || book.pdfUrl.isBlank() || !validUrl(book.pdfUrl)) return reject("invalid_pdf_url")
        if (book.coverUrl != null && !validUrl(book.coverUrl)) return reject("invalid_cover_url")
        if (!Regex("^[a-fA-F0-9]{64}$").matches(book.checksumSha256)) return reject("invalid_checksum")
        if (book.fileSize != null && (book.fileSize <= 0 || book.fileSize > MAX_BOOK_SIZE)) return reject("invalid_file_size")
        val addedAt = parseTimestamp(book.addedAt) ?: return reject("invalid_added_at")
        return BookEntity(book.id, book.title, book.author, book.stream, book.subjectId, book.description, book.coverUrl,
            book.pdfUrl, book.checksumSha256.lowercase(Locale.US), book.fileSize, book.language, book.curriculumYear, addedAt)
    }

    private fun validUrl(value: String): Boolean = try {
        val uri = URI(value)
        val scheme = uri.scheme?.lowercase(Locale.US)
        val releaseValid = scheme == "https"
        val debugValid = releaseValid || (BuildConfig.DEBUG && scheme == "http" && (uri.host == "localhost" || uri.host == "10.0.2.2"))
        debugValid && !scheme.equals("file") && !scheme.equals("content") && !scheme.equals("data")
    } catch (_: Exception) { false }

    private fun parseTimestamp(value: String): Long? = try {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.US).apply { isLenient = false }.parse(value)?.time
    } catch (_: Exception) { null }

    companion object { const val MAX_BOOK_SIZE = 100L * 1024 * 1024 }
}
