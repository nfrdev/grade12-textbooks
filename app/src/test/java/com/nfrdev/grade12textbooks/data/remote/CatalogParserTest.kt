package com.nfrdev.grade12textbooks.data.remote

import com.nfrdev.grade12textbooks.data.remote.dto.BookDto
import com.nfrdev.grade12textbooks.data.remote.dto.CatalogDto
import com.nfrdev.grade12textbooks.util.Logger
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CatalogParserTest {
    private lateinit var parser: CatalogParser
    private val logger = object : Logger { override fun d(message: String) = Unit; override fun e(message: String, throwable: Throwable?) = Unit }
    @Before fun setUp() { parser = CatalogParser(Json { ignoreUnknownKeys = false }, logger) }
    private fun raw(schema: Int = 1, books: List<BookDto> = listOf(book())) = Json.encodeToString(CatalogDto(schema, 1, "2026-01-01T00:00:00Z", books))
    private fun book(id: String = "a", checksum: String = "A".repeat(64)) = BookDto(id, "Book", null, "natural_science", "mathematics", null, null, "https://example.invalid/book.pdf", checksum, 1, "en", null, "2026-01-01T00:00:00Z")

    @Test fun validCatalogAcceptedAndChecksumNormalized() {
        val result = parser.parse(raw()) as CatalogParseResult.Success
        assertEquals("a".repeat(64).lowercase(), result.books.single().checksumSha256)
    }
    @Test fun wrongSchemaRejected() { assertTrue(parser.parse(raw(schema = 2)) is CatalogParseResult.Failure) }
    @Test fun emptyBooksRejected() { assertTrue(parser.parse(raw(books = emptyList())) is CatalogParseResult.Failure) }
    @Test fun missingChecksumRejected() { assertTrue(parser.parse(raw(books = listOf(book(checksum = "")))) is CatalogParseResult.Failure) }
    @Test fun duplicateLaterEntrySkipped() {
        val result = parser.parse(raw(books = listOf(book(), book()))) as CatalogParseResult.Success
        assertEquals(1, result.books.size)
    }
}
