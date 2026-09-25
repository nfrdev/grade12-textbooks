package com.nfrdev.grade12textbooks

import com.nfrdev.grade12textbooks.domain.model.Book
import com.nfrdev.grade12textbooks.domain.model.Stream
import com.nfrdev.grade12textbooks.domain.model.Subject
import com.nfrdev.grade12textbooks.domain.repository.CatalogRepository

class FakeCatalogRepository : CatalogRepository {
    var shouldFail = false
    val books = mutableListOf<Book>()
    override suspend fun refresh() = if (shouldFail) com.nfrdev.grade12textbooks.domain.repository.CatalogResult.Failure("fake_failure") else com.nfrdev.grade12textbooks.domain.repository.CatalogResult.Success(Unit)
    override suspend fun getBooks() = books
    override suspend fun getBooks(stream: Stream, subjectId: String) = books.filter { it.stream == stream && it.subjectId == subjectId }
    override suspend fun getSubjects(stream: Stream): List<Subject> = books.filter { it.stream == stream }.groupBy { it.subjectId }.map { Subject(it.key, it.key, stream, bookCount = it.value.size) }
}

class FakeDownloader
class FakePreferencesDataStore
